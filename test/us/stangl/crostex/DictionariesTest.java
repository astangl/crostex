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
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import us.stangl.crostex.dictionary.Dictionary;
import us.stangl.crostex.dictionary.TST;
import us.stangl.crostex.dictionary.Trie;
import us.stangl.crostex.dictionary.TstNew;
import us.stangl.crostex.dictionary.Xdict;
import us.stangl.crostex.dictionary.Ydict;

/**
 * Check various Dictionary implementations for correctness and speed.
 */
public class DictionariesTest {
	/** logger */
	private static final Logger LOG = Logger.getLogger(YdictTest.class.getName());

	@Test
	public void testSpeed() {
		List<String> patterns = generatePatterns();
		
		populateAndTest(new Ydict(), patterns, "Ydict");
		populateAndTest(new Xdict(), patterns, "Xdict");
		populateAndTest(new Trie(), patterns, "Trie");
		populateAndTest(new TST(), patterns, "TST");
		populateAndTest(new TstNew(), patterns, "TstNew");
	}
	
	private void populateAndTest(Dictionary dict, Collection<String> patterns, String dictName)
	{
		populateDict(dict);
		timePatternMatches(dict, patterns, dictName);
	}
	
	private void timePatternMatches(Dictionary dict, Collection<String> patterns, String dictName)
	{
		long startTime = System.currentTimeMillis();
		long totalMatches = 0;
		for (String pattern : patterns) {
			totalMatches += dict.getPatternMatches(pattern.toCharArray()).size();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Took " + (endTime - startTime) + " ms. for " + dictName + " to return " + totalMatches);
	}

	private void populateDict(Dictionary dict)
	{
		String dataDirectory = "/home/alex/crostex_data";
		boolean dictRead;
		dictRead = readDictionaryFile(dict, dataDirectory, "SINGLE.TXT");
		System.out.println("dictRead = " + dictRead);
		dictRead = readDictionaryFile(dict, dataDirectory, "CROSSWD.TXT");
		System.out.println("dictRead = " + dictRead);
		dictRead = readDictionaryFile(dict, dataDirectory, "CRSWD-D.TXT");
		System.out.println("dictRead = " + dictRead);
		dict.rebalance();
	}
	
	private List<String> generatePatterns()
	{
		long seed = 12345678912l;
		Random prng = new Random(seed);
		
		int numPatterns = 5000;
		
		List<String> words = new ArrayList<String>(numPatterns);
		StringBuilder nextWord = new StringBuilder();
		for (int i = 0; i < numPatterns; ++i) {
			int len = 3 + prng.nextInt(5) + prng.nextInt(5) + prng.nextInt(5);
			nextWord.setLength(0);
			for (int j = 0; j < len; ++j) {
				nextWord.append(prng.nextBoolean() ? '_' : (char)('A' + prng.nextInt(26)));
			}
			words.add(nextWord.toString());
		}
		return words;
	}

	private long testDictReadSpeed(Dictionary dict) {
		
		long seed = 12345678912l;
		Random prng = new Random(seed);
		
		int numPatterns = 5000;
		
		List<String> words = new ArrayList<String>(numPatterns);
		StringBuilder nextWord = new StringBuilder();
		for (int i = 0; i < numPatterns; ++i) {
			int len = 3 + prng.nextInt(5) + prng.nextInt(5) + prng.nextInt(5);
			nextWord.setLength(0);
			for (int j = 0; j < len; ++j) {
				nextWord.append(prng.nextBoolean() ? '_' : (char)('A' + prng.nextInt(26)));
			}
			words.add(nextWord.toString());
		}
		
		long startTime = System.currentTimeMillis();
		long totalMatches = 0;
		for (String pattern : words) {
			totalMatches += dict.getPatternMatches(pattern.toCharArray()).size();
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println("Took " + (endTime - startTime) + " ms. to return " + totalMatches);
		return totalMatches;
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
	
	private boolean readDictionaryFile(Dictionary dict, String dataDirectory, String filename) {
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
					dict.bulkInsert(tempList);
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

}
