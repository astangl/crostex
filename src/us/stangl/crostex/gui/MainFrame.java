/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
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

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridChangeListener;
import us.stangl.crostex.GridFactory;
import us.stangl.crostex.GridsDb;
import us.stangl.crostex.PreferenceKey;
import us.stangl.crostex.PreferencesStore;
import us.stangl.crostex.RomanNumeralGenerator;
import us.stangl.crostex.ServiceException;
import us.stangl.crostex.Word;
import us.stangl.crostex.dictionary.Dictionary;
import us.stangl.crostex.dictionary.Ydict;
import us.stangl.crostex.io.FileReader;
import us.stangl.crostex.io.FileSaver;
import us.stangl.crostex.io.IpuzSerializationException;
import us.stangl.crostex.io.IpuzSerializer;
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

	// File menu option to import IPUZ file
	private JMenuItem importIpuzItem = new JMenuItem(Message.FILE_MENU_OPTION_IMPORT_IPUZ.toString());

	// File menu option to export IPUZ file
	private JMenuItem exportIpuzItem = new JMenuItem(Message.FILE_MENU_OPTION_EXPORT_AS_IPUZ.toString());
	
	// Edit menu option to undo
	private JMenuItem undoItem = newMenuItem(Message.EDIT_MENU_OPTION_UNDO);

	// Edit menu option to redo
	private JMenuItem redoItem = newMenuItem(Message.EDIT_MENU_OPTION_REDO);

	// Edit menu option to set current cell to black
	private JMenuItem setToBlackItem = newMenuItem(Message.EDIT_MENU_OPTION_SET_TO_BLACK);
	
	// Dictionary
	private Dictionary<char[], Word> dict = new Ydict<Word>();
	
	// Preferences store
	private final PreferencesStore preferencesStore = new PreferencesStore();
	
	// width of preferences text fields
	private static final int PREFERENCES_TEXT_FIELD_WIDTH = 40;
	
	public MainFrame() {
		super(Message.MAIN_FRAME_TITLE.toString());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Ensure that dataDirectory preference is set, request it otherwise.
		PreferenceKey dataDirectoryKey = PreferenceKey.DATA_DIRECTORY;
		String dataDirectory = preferencesStore.getValue(dataDirectoryKey, null);
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
						preferencesStore.putValue(dataDirectoryKey, directoryFile.getAbsolutePath());
					}
				} else {
					System.exit(0);
				}
			} else {
				System.exit(0);
			}
			dataDirectory = preferencesStore.getValue(dataDirectoryKey, null);
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
		
		pack();
		setVisible(true);

		// Create menus: File, Edit, Help (mnemonics F, E, H)
		topLevelMenuBar.add(fileMenu());
		topLevelMenuBar.add(editMenu());
		
		// put help menu on right side of menubar
		topLevelMenuBar.add(Box.createHorizontalGlue());
		topLevelMenuBar.add(helpMenu());

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

	// return File menu, F - mnemonic
	private JMenu fileMenu() {
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
		fileMenu.add(importIpuzItem);
		fileMenu.add(exportIpuzItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				NewCrosswordDialog dialog = new NewCrosswordDialog(gridsDb);
				
				LOG.finest("Back from NewCrosswordDialog! width = " + dialog.getGridWidth() + ", height = " + dialog.getGridHeight());
				
				Grid chosenGrid = dialog.getSelectedGridTemplate();
				if (chosenGrid != null) {
					String title = MessageFormat.format(Message.UNTITLED_TAB_TITLE.toString(), untitledTabCounter++);
					Grid gridCopy = new Grid(chosenGrid);
					gridCopy.setTitle(title);
					gridCopy.setAuthor(preferencesStore.getValue(PreferenceKey.DEFAULT_AUTHOR, ""));
					gridCopy.setCopyright(preferencesStore.getValue(PreferenceKey.DEFAULT_COPYRIGHT, ""));
					gridCopy.setNotes(preferencesStore.getValue(PreferenceKey.DEFAULT_NOTES, ""));
					openGridInNewTab(gridCopy);
				}
				saveAsTemplate.setEnabled(true);
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
		
		importPuzItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
						LOG.log(Level.WARNING, "IOException caught trying to import PUZ file " + file, e);
					}
				}
			}
		});
		exportPuzItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel crosswordPanel = getCrosswordPanel();
				if (crosswordPanel != null) {
					JFileChooser fileChooser = new FileChooserConfirmingOverwrite(); //saveFileChooserPromptingOverwrite(); //new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
					{
						byte[] bytes = new PuzSerializer().toPuz(crosswordPanel.getGrid());
						FileSaver.saveToFile(bytes, fileChooser.getSelectedFile());
					}
				}
			}
		});
		importIpuzItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
				{
					File file = fileChooser.getSelectedFile();
					try {
						byte[] bytes = FileReader.getFileBytes(file);
						Grid grid = new IpuzSerializer().fromBytes(bytes, new GridFactory());
						if (grid == null) {
							LOG.severe("Couldn't open IPUZ file " + file + " because it appears invalid");
						} else {
							openGridInNewTab(grid);
						}
					} catch (IpuzSerializationException e) {
						LOG.log(Level.WARNING, "IpuzSerializationException caught trying to import IPUZ file " + file, e);
					} catch (IOException e) {
						LOG.log(Level.WARNING, "IOException caught trying to import IPUZ file " + file, e);
					}
				}
			}
		});
		exportIpuzItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel crosswordPanel = getCrosswordPanel();
				if (crosswordPanel != null) {
					JFileChooser fileChooser = new FileChooserConfirmingOverwrite(); //saveFileChooserPromptingOverwrite(); //new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
					{
						try {
							byte[] bytes = new IpuzSerializer().toBytes(crosswordPanel.getGrid());
							File fileToWrite = fileChooser.getSelectedFile();
							if (fileToWrite.exists()) {
								
							}
							FileSaver.saveToFile(bytes, fileToWrite);
						} catch (IpuzSerializationException e) {
							LOG.log(Level.WARNING, "IpuzSerializationException caught trying to export IPUZ file", e);
						}
					}
				}
			}
		});
		
		return fileMenu;
	}

	// return Edit menu, E - mnemonic
	private JMenu editMenu() {
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
					grid.setCellBlackCommand();
					resetMenuState();
					crosswordPanel.repaint(0);
				}
			}
		});
		setToBlackItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));

		JMenuItem preferencesItem = newMenuItem(Message.EDIT_MENU_OPTION_PREFERENCES);
		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showPreferencesDialog();
			}
		});

		JMenu editMenu = new JMenu(Message.EDIT_MENU_HEADER.toString());
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.add(undoItem);
		editMenu.add(redoItem);
		editMenu.addSeparator();
		editMenu.add(setToBlackItem);
		editMenu.addSeparator();
		editMenu.add(preferencesItem);
		return editMenu;
	}

	// return Help menu - H mnemonic
	private JMenu helpMenu() {
		JMenu helpMenu = new JMenu(Message.HELP_MENU_HEADER.toString());
		helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem helpContentsItem = new JMenuItem(Message.HELP_MENU_OPTION_HELP_CONTENTS.toString());
		helpContentsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JEditorPane editorPane = new JEditorPane();
				editorPane.setContentType("text/html");
				editorPane.setPreferredSize(new Dimension(520,180));
				//editorPane.setSize(400, 400);
				//editorPane.
				//editorPane.setText("<html><head></head><body><b>This is a test!</b></body></html>");
				editorPane.setText(Message.HELP_HTML_CONTENTS.toString());
				JOptionPane.showMessageDialog(MainFrame.this,
					new JScrollPane(editorPane),
					Message.HELP_MENU_OPTION_HELP_CONTENTS.toString(),
					JOptionPane.PLAIN_MESSAGE, new CrosswordIcon(5));
			}
		});
		helpMenu.add(helpContentsItem);
		helpMenu.addSeparator();
		JMenuItem aboutItem = new JMenuItem(Message.HELP_MENU_OPTION_ABOUT.toString());
		aboutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JOptionPane.showMessageDialog(MainFrame.this,
					Message.HELP_ABOUT_DIALOG_TEXT.toString(),
					Message.HELP_MENU_OPTION_ABOUT.toString(),
					JOptionPane.PLAIN_MESSAGE, new CrosswordIcon(5));
			}
		});
		helpMenu.add(aboutItem);
		return helpMenu;
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
	
	private void showPreferencesDialog() {
		final JDialog dialog = new JDialog(MainFrame.this, Message.DIALOG_TITLE_PREFERENCES.toString(), true);
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		final JTextField dataDirectoryField = new JTextField(
				preferencesStore.getValue(PreferenceKey.DATA_DIRECTORY, ""), PREFERENCES_TEXT_FIELD_WIDTH);
		final JTextField defaultAuthorField = new JTextField(
				preferencesStore.getValue(PreferenceKey.DEFAULT_AUTHOR, ""), PREFERENCES_TEXT_FIELD_WIDTH);
		final JTextField defaultCopyrightField = new JTextField(
				preferencesStore.getValue(PreferenceKey.DEFAULT_COPYRIGHT, ""), PREFERENCES_TEXT_FIELD_WIDTH);
		final JTextField defaultNotesField = new JTextField(
				preferencesStore.getValue(PreferenceKey.DEFAULT_NOTES, ""), PREFERENCES_TEXT_FIELD_WIDTH);

		panel.add(Message.LABEL_DATA_DIRECTORY.label(), new GBC(0, 0).anchor(GBC.NORTHWEST));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(dataDirectoryField, new GBC(2, 0).anchor(GBC.NORTHWEST).weightx(1.0).gridwidth(GBC.REMAINDER));
		panel.add(Message.LABEL_DEFAULT_AUTHOR.label(), new GBC(0, 1).anchor(GBC.NORTHWEST));
		panel.add(defaultAuthorField, new GBC(2, 1).anchor(GBC.NORTHWEST).weightx(1.0).gridwidth(GBC.REMAINDER));
		panel.add(Message.LABEL_DEFAULT_COPYRIGHT.label(), new GBC(0, 2).anchor(GBC.NORTHWEST));
		panel.add(defaultCopyrightField, new GBC(2, 2).anchor(GBC.NORTHWEST).weightx(1.0).gridwidth(GBC.REMAINDER));
		panel.add(Message.LABEL_DEFAULT_NOTES.label(), new GBC(0, 3).anchor(GBC.NORTHWEST));
		panel.add(defaultNotesField, new GBC(2, 3).anchor(GBC.NORTHWEST).weightx(1.0).gridwidth(GBC.REMAINDER));
		Container dialogContentPane = dialog.getContentPane();
		JPanel buttonsPanel = new JPanel();
		JButton okButton = new JButton(Message.BUTTON_OK.toString());
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				preferencesStore.putValue(PreferenceKey.DATA_DIRECTORY, dataDirectoryField.getText());
				preferencesStore.putValue(PreferenceKey.DEFAULT_AUTHOR, defaultAuthorField.getText());
				preferencesStore.putValue(PreferenceKey.DEFAULT_COPYRIGHT, defaultCopyrightField.getText());
				preferencesStore.putValue(PreferenceKey.DEFAULT_NOTES, defaultNotesField.getText());
				dialog.dispose();
			}
		});
		buttonsPanel.add(okButton);
		JButton cancelButton = new JButton(Message.BUTTON_CANCEL.toString());
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				dialog.dispose();
			}
		});
		buttonsPanel.add(cancelButton);
		dialogContentPane.add(panel, BorderLayout.CENTER);
		dialogContentPane.add(buttonsPanel, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.this);
		dialog.setVisible(true);
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
