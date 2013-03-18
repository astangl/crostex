/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridChangeListener;
import us.stangl.crostex.GridFactory;
import us.stangl.crostex.GridsDb;
import us.stangl.crostex.Main;
import us.stangl.crostex.RomanNumeralGenerator;
import us.stangl.crostex.ServiceException;
import us.stangl.crostex.Word;
import us.stangl.crostex.dictionary.Dictionary;
import us.stangl.crostex.dictionary.Ydict;
import us.stangl.crostex.io.FileReader;
import us.stangl.crostex.io.FileSaver;
import us.stangl.crostex.io.PuzSerializer;
import us.stangl.crostex.util.Message;
import us.stangl.crostex.util.Pair;

/**
 * Main GUI frame.
 * @author Alex Stangl
 */
public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	// logger
	private static final Logger LOG = Logger.getLogger(MainFrame.class.getName());

	// preferred size dimensions
	private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);
	
	// map of grid templates,  name -> template
	private GridsDb gridsDb;
	
	// top-level tabbed pane
	private JTabbedPane topLevelTabbedPane = new JTabbedPane();
	
	/** counter for numbering Untitled tabs */
	private int untitledTabCounter = 1;
	
	// top level menu bar
	private JMenuBar topLevelMenuBar = new JMenuBar();
	
	// File menu option to save grid as template
	private JMenuItem saveAsTemplate = newMenuItem(Message.FILE_MENU_OPTION_SAVE_GRID_AS_TEMPLATE);
	
	// File menu option to import PUZ file
	private JMenuItem importPuzItem = new JMenuItem(Message.FILE_MENU_OPTION_IMPORT_PUZ.toString());
	
	// File menu option to export PUZ file
	private JMenuItem exportPuzItem = new JMenuItem(Message.FILE_MENU_OPTION_EXPORT_AS_PUZ.toString());

	// Edit menu option to undo
	private JMenuItem undoItem = newMenuItem(Message.EDIT_MENU_OPTION_UNDO);

	// Edit menu option to redo
	private JMenuItem redoItem = newMenuItem(Message.EDIT_MENU_OPTION_REDO);

	// Edit menu option to set current cell to black
	private JMenuItem setToBlackItem = newMenuItem(Message.EDIT_MENU_OPTION_SET_TO_BLACK);
	
	// Dictionary
	private Dictionary<char[], Word> dict = new Ydict<Word>();
	
	public MainFrame() {
		super(Message.MAIN_FRAME_TITLE.toString());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Preferences p = Preferences.userNodeForPackage(Main.class);
		
		// Ensure that dataDirectory preference is set, request it otherwise.
		String dataDirectoryPropertyName = "dataDirectory";
		//p.remove(dataDirectoryPropertyName);
		String dataDirectory = p.get(dataDirectoryPropertyName, null);
		while (dataDirectory == null) {
			int answer = JOptionPane.showConfirmDialog(this,
					Message.DIALOG_TEXT_SET_DATA_DIRECTORY.toString(),
					Message.DIALOG_TITLE_SET_DATA_DIRECTORY.toString(),
					JOptionPane.OK_CANCEL_OPTION);
			if (answer == JOptionPane.OK_OPTION) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int state = fileChooser.showDialog(this, Message.BUTTON_PICK_DATA_DIRECTORY.toString());
				if (state == JFileChooser.APPROVE_OPTION) {
					File directoryFile = fileChooser.getSelectedFile();
					LOG.fine("Chose data directory " + directoryFile.getAbsolutePath());
					if (! directoryFile.isFile()) {
						p.put(dataDirectoryPropertyName, directoryFile.getAbsolutePath());
					}
				} else {
					System.exit(0);
				}
			} else {
				System.exit(0);
			}
			dataDirectory = p.get(dataDirectoryPropertyName, null);
		}
		
		// ... and ensure data directory exists, offering to create it otherwise
		File directoryFile = new File(dataDirectory);
		if (! directoryFile.isDirectory()) {
			String msg = MessageFormat.format(Message.DIALOG_TEXT_CONFIRM_DATA_DIRECTORY_CREATE.toString(), dataDirectory);
			int confirmAnswer = JOptionPane.showConfirmDialog(this,
					msg, Message.DIALOG_TITLE_CONFIRM_DATA_DIRECTORY_CREATE.toString(),
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (confirmAnswer == JOptionPane.YES_OPTION) {
				if (! directoryFile.mkdirs()) {
					String errorMsg = MessageFormat.format(Message.DIALOG_TEXT_UNABLE_TO_CREATE_DATA_DIRECTORY.toString(),
							directoryFile.getAbsolutePath());
					JOptionPane.showMessageDialog(this, errorMsg, Message.DIALOG_TITLE_UNABLE_TO_CREATE_DATA_DIRECTORY.toString(), JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		}
		
		// Read all grid templates from the grids database
		try {
			gridsDb = GridsDb.read(dataDirectory);
		} catch (ServiceException e) {
			LOG.log(Level.SEVERE, "ServiceException caught", e);
		}
		
		// Read in dictionaries
		int nbrDictionariesRead = 0;
//		if (readDictionaryFile(dataDirectory, "SINGLE.TXT"))
//			++nbrDictionariesRead;
		if (readDictionaryFile(dataDirectory, "CROSSWD.TXT"))
			++nbrDictionariesRead;
		if (readDictionaryFile(dataDirectory, "CRSWD-D.TXT"))
			++nbrDictionariesRead;
		LOG.info("Read " + nbrDictionariesRead + " dictionaries");
		
		// Add all roman numerals to dictionary
		RomanNumeralGenerator romanNumeralGenerator = new RomanNumeralGenerator();
		Word dummyWord = new Word();
		for (int len = 1; len <= 15; ++len)
			for (String numeral : romanNumeralGenerator.generateAllNumeralsOfLength(len))
				dict.insert(numeral.toCharArray(), dummyWord);
		dict.rebalance();
		
		
		topLevelTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent evt) {
				resetMenuState();
			}
		});
		add(topLevelTabbedPane);
		
//		add(new CrosswordPanel());
		pack();
		setVisible(true);

//		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		f.addWindowListener(new WindowAdapter() {
//			public void windowClosed(WindowEvent e) {
//				System.exit(0);
//			}
//		});
		
		// Create menus
		// File Menu, F - Mnemonic
		JMenu fileMenu = new JMenu(Message.FILE_MENU_HEADER.toString());
		fileMenu.setMnemonic(KeyEvent.VK_F);

		JMenuItem newItem = new JMenuItem(Message.FILE_MENU_OPTION_NEW.toString());
		saveAsTemplate.setEnabled(false);
		JMenuItem exitItem = new JMenuItem(Message.FILE_MENU_OPTION_EXIT.toString());
		fileMenu.add(newItem);
		fileMenu.add(saveAsTemplate);
		fileMenu.add(importPuzItem);
		fileMenu.add(exportPuzItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		newItem.setActionCommand("Create new crossword tableau");
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				NewCrosswordDialog dialog = new NewCrosswordDialog(gridsDb);
				
				LOG.finest("Back from NewCrosswordDialog! width = " + dialog.getGridWidth() + ", height = " + dialog.getGridHeight());
				
				Grid chosenGrid = dialog.getSelectedGridTemplate();
				if (chosenGrid != null) {
//					chosenGrid = new Grid(5, 5, "dummy", "dummy");
					// Temporarily do auto-fill here
//					trie_.insert("AAA".toCharArray(), new Word());
//					long startTime = System.currentTimeMillis();
//					boolean autofillReturn = chosenGrid.autoFill(dict_);
//					long endTime = System.currentTimeMillis();
//					System.out.println("autofill returns " + autofillReturn + ", elapsed time " + (endTime - startTime) + " ms.");

					String title = MessageFormat.format(Message.UNTITLED_TAB_TITLE.toString(), untitledTabCounter++);
					Grid gridCopy = new Grid(chosenGrid);
					gridCopy.setTitle(title);
					openGridInNewTab(gridCopy);
					
//					gridCopy.addTitleChangeListener(new GridChangeListener() {
//						public void handleChange(Grid grid) {
//							topLevelTabbedPane.setTitleAt(topLevelTabbedPane.getSelectedIndex(), grid.getTitle());
//						}
//					});
//					//CrosswordPanel crosswordPanel = new CrosswordPanel(MainFrame.this, gridCopy);
//					//topLevelTabbedPane.addTab(tabTitle, crosswordPanel);
////					topLevelTabbedPane.addTab(tabTitle, new CrosswordPanel(MainFrame.this, gridCopy));
//					topLevelTabbedPane.addTab(title, new TopLevelTabPanel(MainFrame.this, gridCopy));
//					topLevelTabbedPane.setSelectedIndex(topLevelTabbedPane.getTabCount() - 1);
////					crosswordPanel.setFocusable(true);
////					crosswordPanel.requestFocusInWindow();
				}
				saveAsTemplate.setEnabled(true);
//				tabbedPane_.addTab(title, component);
				
//				// create new non-modal dialog box
//				JPanel buttonsPanel = new JPanel();
//				buttonsPanel.add(new JButton(Message.BUTTON_OK.toString()));
//				buttonsPanel.add(new JButton(Message.BUTTON_CANCEL.toString()));
//				
//				JDialog dialog = new JDialog((Frame)null, Message.DIALOG_TITLE_NEW_CROSSWORD.toString(), false);
//				dialog.getContentPane().add(new NewCrosswordPanel(), BorderLayout.CENTER);
//				dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
//				dialog.pack();
//				dialog.setVisible(true);
			}
		});
		
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.exit(0);
			}
		});

		saveAsTemplate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel crosswordPanel = getCrosswordPanel();
				Grid gridCopy = new Grid(crosswordPanel.getGrid());
				SaveGridTemplateDialog dialog = new SaveGridTemplateDialog(gridsDb, gridCopy);
				JTextField nameField = dialog.getNameField();
				JTextField descriptionField = dialog.getDescriptionField();
				if (nameField != null && descriptionField != null) {
					LOG.fine("done with SaveGridTemplateDialog, name = " + dialog.getNameField().getText() + ", description = " +dialog.getDescriptionField().getText());
				}
			}
		});
		
		importPuzItem.setActionCommand("Import PUZ file");
		importPuzItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser fileChooser = new JFileChooser();
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();
					try {
						byte[] bytes = FileReader.getFileBytes(file);
						Grid grid = new PuzSerializer().fromBytes(bytes, new GridFactory(), true);
						if (grid == null) {
							LOG.severe("Couldn't open PUZ file " + file + " because it appears invalid");
						} else {
							openGridInNewTab(grid);
						}
					} catch (IOException e) {
						LOG.severe("IOException caught trying to import PUZ file " + file + ": " + e);
					}
				}
			}
		});
		exportPuzItem.setActionCommand("Export as PUZ file");
		exportPuzItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel crosswordPanel = getCrosswordPanel();
				if (crosswordPanel != null) {
					JFileChooser fileChooser = new JFileChooser();
					if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
					{
						byte[] bytes = new PuzSerializer().toPuz(crosswordPanel.getGrid());
						FileSaver.saveToFile(bytes, fileChooser.getSelectedFile());
					}
				}
			}
		});

		topLevelMenuBar.add(fileMenu);
		
		undoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel crosswordPanel = getCrosswordPanel();
				if (crosswordPanel != null) {
					Grid grid = crosswordPanel.getGrid();
					grid.undo();
					resetMenuState();
					crosswordPanel.repaint(0);
				}
			}
		});
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		
		redoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel crosswordPanel = getCrosswordPanel();
				if (crosswordPanel != null) {
					Grid grid = crosswordPanel.getGrid();
					grid.redo();
					resetMenuState();
					crosswordPanel.repaint(0);
				}
			}
		});
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
		
		setToBlackItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel crosswordPanel = getCrosswordPanel();
				if (crosswordPanel != null) {
					Grid grid = crosswordPanel.getGrid();
					grid.setCurrentCellBlack();
					resetMenuState();
					crosswordPanel.repaint(0);
				}
			}
		});
		setToBlackItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));

		JMenuItem preferencesItem = newMenuItem(Message.EDIT_MENU_OPTION_PREFERENCES);
		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
			}
		});

		// Edit Menu, E - Mnemonic
		JMenu editMenu = new JMenu(Message.EDIT_MENU_HEADER.toString());
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.add(undoItem);
		editMenu.add(redoItem);
		editMenu.addSeparator();
		editMenu.add(setToBlackItem);
		editMenu.addSeparator();
		editMenu.add(preferencesItem);
		topLevelMenuBar.add(editMenu);
		
		// put help menu on right side of menubar
		topLevelMenuBar.add(Box.createHorizontalGlue());

		// Help Menu, H - Mnemonic
		JMenu helpMenu = new JMenu(Message.HELP_MENU_HEADER.toString());
		helpMenu.setMnemonic(KeyEvent.VK_H);
		topLevelMenuBar.add(helpMenu);

		getRootPane().setJMenuBar(topLevelMenuBar);
	}
	

	/**
	 * @return preferred size
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}
	
	// normalize raw word from dictionary; return null if word is unacceptable
	private String normalizeWord(String rawWord) {
		int len = rawWord.length();
		if (len < 3)
			return null;
		StringBuilder builder = new StringBuilder(rawWord.length());
		for (int i = 0; i < len; ++i) {
			char c = rawWord.charAt(i);
			if (c >= 'a' && c <= 'z')
				c = Character.toUpperCase(c);
			if (c < 'A' || c > 'Z')
				return null;
			builder.append(c);
		}
		return builder.toString();
	}
	
	private boolean readDictionaryFile(String dataDirectory, String filename) {
		File dictionaryFile = new File(dataDirectory, filename);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(dictionaryFile), "UTF-8"));
			List<Pair<char[], Word>> tempList = new ArrayList<Pair<char[], Word>>(100000);
			while (true) {
				String rawWord = in.readLine();
				if (rawWord == null) {
					dict.bulkInsert(tempList);
					LOG.info("Successfully read dictionary " + dictionaryFile);
					return true;
				}
				String normalizedWord = normalizeWord(rawWord);
				if (normalizedWord != null) {
					tempList.add(new Pair<char[], Word>(normalizedWord.toCharArray(), new Word()));
				}
			}
		} catch (FileNotFoundException e) {
			LOG.log(Level.SEVERE, "Unable to open dictionary file " + dictionaryFile, e);
			return false;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "IOException caught trying to read dictionary file " + dictionaryFile, e);
			return false;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					LOG.log(Level.WARNING, "Caught IOException trying to close dictionary in finally", e);
				}
		}
	}
	
	// return new JMenuItem for the specified Message
	private JMenuItem newMenuItem(Message message) {
		return new JMenuItem(message.toString());
	}
	
	// open specified grid in new tab
	private void openGridInNewTab(Grid grid) {
		grid.addTitleChangeListener(new GridChangeListener() {
			public void handleChange(Grid grid) {
				topLevelTabbedPane.setTitleAt(topLevelTabbedPane.getSelectedIndex(), grid.getTitle());
			}
		});
		topLevelTabbedPane.addTab(grid.getTitle(), new TopLevelTabPanel(MainFrame.this, grid));
		topLevelTabbedPane.setSelectedIndex(topLevelTabbedPane.getTabCount() - 1);
	}
	
	/**
	 * Set undo/redo enabled/disabled, etc. based upon whether these operation can be performed on current CrosswordPuzzle.
	 */
	public void resetMenuState() {
		CrosswordPanel crosswordPanel = getCrosswordPanel();
		if (crosswordPanel == null) {
			undoItem.setEnabled(false);
			redoItem.setEnabled(false);
			exportPuzItem.setEnabled(false);
			
		} else {
			Grid grid = crosswordPanel.getGrid();
			undoItem.setEnabled(grid.isAbleToUndo());
			redoItem.setEnabled(grid.isAbleToRedo());
			exportPuzItem.setEnabled(grid.isReadyToExport());
		}
	}

	// return currently selected CrosswordPanel, if any, else null
	private CrosswordPanel getCrosswordPanel() {
		TopLevelTabPanel tabPanel = getTopLevelTabPanel();
		return tabPanel == null ? null : tabPanel.getCrosswordPanel();
	}
	
	// return currently selected top-level tab panel, if any, else null
	private TopLevelTabPanel getTopLevelTabPanel() {
		return (TopLevelTabPanel)topLevelTabbedPane.getSelectedComponent();
	}
}
