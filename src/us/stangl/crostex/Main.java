/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import us.stangl.crostex.dictionary.Dictionary;
import us.stangl.crostex.dictionary.Ydict;
import us.stangl.crostex.gui.MainFrame;

/**
 * Main class to startup application.
 */
public class Main {
	/** set flag to true for profiling autofill */
	private static final boolean PROFILING = false;
	/**
	 * Main entry point, starts application running on EDT thread.
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		if (PROFILING) {
			new Profiler().profileTest();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					new MainFrame();
				}
			});
		}
	}
	
	
	private static class Profiler {
		/** Dictionary */
		private Dictionary dict_ = new Ydict();
		
		public void profileTest() {
			Preferences p = Preferences.userNodeForPackage(Main.class);
			
			// Ensure that dataDirectory preference is set, request it otherwise.
			String dataDirectoryPropertyName = "dataDirectory";
			//p.remove(dataDirectoryPropertyName);
			String dataDirectory = p.get(dataDirectoryPropertyName, null);
			
			// ... and ensure data directory exists, offering to create it otherwise
			File directoryFile = new File(dataDirectory);
			
			// Read in dictionary trie
			boolean dictRead;
//			dictRead = readDictionaryFile(dataDirectory, "SINGLE.TXT");
//			System.out.println("dictRead = " + dictRead);
			dictRead = readDictionaryFile(dataDirectory, "CROSSWD.TXT");
			System.out.println("dictRead = " + dictRead);
			dictRead = readDictionaryFile(dataDirectory, "CRSWD-D.TXT");
			System.out.println("dictRead = " + dictRead);
			dict_.rebalance();
			
			Grid chosenGrid = new Grid(5, 6, "dummy", "dummy");
			// Temporarily do auto-fill here
//				trie_.insert("AAA".toCharArray(), new Word());
			boolean autofillReturn = chosenGrid.autoFill(dict_);
			
		}


		/** normalize raw word from dictionary; return null if word is unacceptable */
		private static String normalizeWord(String rawWord) {
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
//			dict_.insert("ABCD".toCharArray(), new Word());
//			dict_.insert("BCDA".toCharArray(), new Word());
//			dict_.insert("CDAB".toCharArray(), new Word());
//			dict_.insert("DABC".toCharArray(), new Word());
//			return true;
			try {
//				dict_.insert("XXX".toCharArray(), new Word());
//				dict_.insert("XXXX".toCharArray(), new Word());
//				dict_.insert("YAM".toCharArray(), new Word());
//				dict_.insert("ZOO".toCharArray(), new Word());
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
//						dict_.insert(normalizedWord.toCharArray(), new Word());
						
//	if (normalizedWord.length() == 3) {
//		String pattern = normalizedWord.charAt(0) + "__";
//		System.out.println("INSERTING " + normalizedWord + ", dict says " + dict_.isPatternInDictionary(pattern.toCharArray()) + " for " + pattern);
//	}
					}
				}
			} catch (FileNotFoundException e) {
				System.err.println("Unable to open dictionary file " + dictionaryFile + ": " + e);
				return false;
			} catch (IOException e) {
				System.err.println("IOException caught trying to read dictionary file " + dictionaryFile + ": " + e);
				return false;
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						System.err.println("Caught IOException trying to close dictionary in finally" + ": " + e);
					}
			}
		}
	}
}
