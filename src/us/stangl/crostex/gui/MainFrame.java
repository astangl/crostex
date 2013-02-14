/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import us.stangl.crostex.Dictionary;
import us.stangl.crostex.Grid;
import us.stangl.crostex.GridsDb;
import us.stangl.crostex.Main;
import us.stangl.crostex.Pair;
import us.stangl.crostex.RomanNumeralGenerator;
import us.stangl.crostex.ServiceException;
import us.stangl.crostex.Word;
import us.stangl.crostex.Ydict;
import us.stangl.crostex.util.Message;

/**
 * Main GUI frame.
 */
public class MainFrame extends JFrame {
	/** logger */
	private static final Logger LOG = Logger.getLogger(MainFrame.class.getName());

	/** preferred size dimensions */
	private static final Dimension PREFERRED_SIZE = new Dimension(800, 600);
	
	/** map of grid templates,  name -> template */
	private GridsDb gridsDb_;
//	private Map<String, Grid> gridTemplates_ = new HashMap<String, Grid>();
	
	/** tabbed pane */
	private JTabbedPane tabbedPane_;
	
	/** counter for numbering Untitled tabs */
	private int untitledTabCounter_ = 1;
	
	/** pseudorandom number generator */
	private Random prng_ = new Random();

	/** File menu option to save grid as template */
	private JMenuItem saveAsTemplate_ = new JMenuItem(Message.FILE_MENU_OPTION_SAVE_GRID_AS_TEMPLATE.toString());

	/** Dictionary */
	private Dictionary<char[], Word> dict_ = new Ydict<Word>();
	
	public MainFrame() {
		super(Message.MAIN_FRAME_TITLE.toString());
//		System.out.println("Created GUI on EDT? " + SwingUtilities.isEventDispatchThread());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Preferences p = Preferences.userNodeForPackage(Main.class);
		
		// Ensure that dataDirectory preference is set, request it otherwise.
		String dataDirectoryPropertyName = "dataDirectory";
		//p.remove(dataDirectoryPropertyName);
		String dataDirectory = p.get(dataDirectoryPropertyName, null);
		while (dataDirectory == null) {
			System.out.println("dataDirectory = " + dataDirectory);
			int answer = JOptionPane.showConfirmDialog(this,
					Message.DIALOG_TEXT_SET_DATA_DIRECTORY.toString(),
					Message.DIALOG_TITLE_SET_DATA_DIRECTORY.toString(),
					JOptionPane.OK_CANCEL_OPTION);
			if (answer == JOptionPane.OK_OPTION) {
				System.out.println("OK option chosen");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int state = fileChooser.showDialog(this, Message.BUTTON_PICK_DATA_DIRECTORY.toString());
				if (state == JFileChooser.APPROVE_OPTION) {
					File directoryFile = fileChooser.getSelectedFile();
					System.out.println("Chose " + directoryFile.getAbsolutePath());
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
			gridsDb_ = GridsDb.read(dataDirectory);
		} catch (ServiceException e) {
			LOG.log(Level.SEVERE, "ServiceException caught", e);
		}
//		gridTemplates_ = readGridDb(dataDirectory);
		
		// Read in dictionary trie
		boolean dictRead;
//		dictRead = readDictionaryFile(dataDirectory, "SINGLE.TXT");
//		System.out.println("dictRead = " + dictRead);
		dictRead = readDictionaryFile(dataDirectory, "CROSSWD.TXT");
		System.out.println("dictRead = " + dictRead);
		dictRead = readDictionaryFile(dataDirectory, "CRSWD-D.TXT");
		System.out.println("dictRead = " + dictRead);
		
		// Add all roman numerals to dictionary
		RomanNumeralGenerator romanNumeralGenerator = new RomanNumeralGenerator();
		Word dummyWord = new Word();
		for (int len = 1; len <= 15; ++len)
			for (String numeral : romanNumeralGenerator.generateAllNumeralsOfLength(len))
				dict_.insert(numeral.toCharArray(), dummyWord);
		dict_.rebalance();
		
		
		
		tabbedPane_ = new JTabbedPane();
		add(tabbedPane_);
		
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
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu(Message.FILE_MENU_HEADER.toString());
		
		JMenuItem newItem = new JMenuItem(Message.FILE_MENU_OPTION_NEW.toString());
		saveAsTemplate_.setEnabled(false);
		JMenuItem exitItem = new JMenuItem(Message.FILE_MENU_OPTION_EXIT.toString());
		fileMenu.add(newItem);
		fileMenu.add(saveAsTemplate_);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		newItem.setActionCommand("Create new crossword tableau");
		newItem.addActionListener(new NewActionListener());
		
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.exit(0);
			}
		});

		saveAsTemplate_.addActionListener(new SaveAsTemplateActionListener());
		
//		fileMenu.add(new NewAction());
		menuBar.add(fileMenu);
		getRootPane().setJMenuBar(menuBar);
	}
	
	/** normalize raw word from dictionary; return null if word is unacceptable */
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
//		for (char c : rawWord) {
//			
//		}
//		String uppercaseWord = rawWord.toUpperCase();
//		if (! uppercaseWord.matches("[A-Z]*")) {
////			System.out.println("Not using " + uppercaseWord);
//			return null;
//		}
//		return uppercaseWord;
	}
	
	private boolean readDictionaryFile(String dataDirectory, String filename) {
		File dictionaryFile = new File(dataDirectory, filename);
		BufferedReader in = null;
//		dict_.insert("ABCD".toCharArray(), new Word());
//		dict_.insert("BCDA".toCharArray(), new Word());
//		dict_.insert("CDAB".toCharArray(), new Word());
//		dict_.insert("DABC".toCharArray(), new Word());
//		return true;
		try {
//			dict_.insert("XXX".toCharArray(), new Word());
//			dict_.insert("XXXX".toCharArray(), new Word());
//			dict_.insert("YAM".toCharArray(), new Word());
//			dict_.insert("ZOO".toCharArray(), new Word());
			in = new BufferedReader(new FileReader(dictionaryFile));
			List<Pair<char[], Word>> tempList = new ArrayList<Pair<char[], Word>>(100000);
			while (true) {
				String rawWord = in.readLine();
				if (rawWord == null) {
					dict_.bulkInsert(tempList);
					return true;
				}
				String normalizedWord = normalizeWord(rawWord);
				if (normalizedWord != null) {
					tempList.add(new Pair(normalizedWord.toCharArray(), new Word()));
//					dict_.insert(normalizedWord.toCharArray(), new Word());
					
//if (normalizedWord.length() == 3) {
//	String pattern = normalizedWord.charAt(0) + "__";
//	System.out.println("INSERTING " + normalizedWord + ", dict says " + dict_.isPatternInDictionary(pattern.toCharArray()) + " for " + pattern);
//}
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
	private class NewActionListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			System.out.println("Clicked new!");
			
			NewCrosswordDialog dialog = new NewCrosswordDialog(gridsDb_);
			int height = dialog.getGridHeight();
			int width = dialog.getGridWidth();
			if (height != -1 && width != -1) {
				new PickGridDialog(height, width);
			}
			
			LOG.finest("Back from NewCrosswordDialog! width = " + dialog.getGridWidth() + ", height = " + dialog.getGridHeight());
			
			// Use copy of randomly-chosen grid template
//			int index = prng_.nextInt(gridTemplates_.size());
//			System.out.println("Copying grid " + gridTemplates_.get(index));
			Grid chosenGrid = dialog.getSelectedGridTemplate();
			if (chosenGrid != null) {
//				chosenGrid = new Grid(5, 5, "dummy", "dummy");
				// Temporarily do auto-fill here
//				trie_.insert("AAA".toCharArray(), new Word());
//				long startTime = System.currentTimeMillis();
//				boolean autofillReturn = chosenGrid.autoFill(dict_);
//				long endTime = System.currentTimeMillis();
//				System.out.println("autofill returns " + autofillReturn + ", elapsed time " + (endTime - startTime) + " ms.");

				String tabTitle = MessageFormat.format(Message.UNTITLED_TAB_TITLE.toString(), untitledTabCounter_++);
				Grid gridCopy = new Grid(chosenGrid);
				tabbedPane_.addTab(tabTitle, new CrosswordPanel(gridCopy));
				tabbedPane_.setSelectedIndex(tabbedPane_.getTabCount() - 1);
			}
			saveAsTemplate_.setEnabled(true);
//			tabbedPane_.addTab(title, component);
			
//			// create new non-modal dialog box
//			JPanel buttonsPanel = new JPanel();
//			buttonsPanel.add(new JButton(Message.BUTTON_OK.toString()));
//			buttonsPanel.add(new JButton(Message.BUTTON_CANCEL.toString()));
//			
//			JDialog dialog = new JDialog((Frame)null, Message.DIALOG_TITLE_NEW_CROSSWORD.toString(), false);
//			dialog.getContentPane().add(new NewCrosswordPanel(), BorderLayout.CENTER);
//			dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
//			dialog.pack();
//			dialog.setVisible(true);
		}
	}
	
	private class SaveAsTemplateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			System.out.println("in actionPerformed");
			
			CrosswordPanel selectedPanel = (CrosswordPanel)tabbedPane_.getSelectedComponent();
			Grid gridCopy = new Grid(selectedPanel.getGrid());
			SaveGridTemplateDialog dialog = new SaveGridTemplateDialog(gridsDb_, gridCopy);
			dialog.getName();
			JTextField nameField = dialog.getNameField();
			JTextField descriptionField = dialog.getDescriptionField();
			if (nameField != null && descriptionField != null) {
				String name = nameField.getText();
				String description = descriptionField.getText();
				System.out.println("done with SaveGridTemplateDialog, name = " + dialog.getNameField().getText() + ", description = " +dialog.getDescriptionField().getText());
//				for (Grid grid : gridsDb_.getGrids()) {
//					if (grid.getName() == name)
//				}
			}
		}
	}

	/**
	 * @return preferred size
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}
}
