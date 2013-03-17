/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import us.stangl.crostex.dictionary.Dictionary;
import us.stangl.crostex.dictionary.TST;
import us.stangl.crostex.dictionary.Trie;
import us.stangl.crostex.dictionary.TstNew;
import us.stangl.crostex.dictionary.Ydict;
import us.stangl.crostex.util.Pair;
import us.stangl.crostex.util.ResettableIterator;

/**
 * JUnit tests for Trie.
 * @author Alex Stangl
 */
public class TrieTest {
	@Test
	public void testTiny() {
		testTinyImpl(new TST<Word>());
		testTinyImpl(new Trie<Word>());
		testTinyImpl(new Ydict<Word>());
		testTinyImpl(new TstNew<Word>());
	}
	
	@Test
	public void testBasicOperation() {
		testBasicOperationImpl(new TST<Word>());
		testBasicOperationImpl(new Trie<Word>());
		testBasicOperationImpl(new Ydict<Word>());
		testBasicOperationImpl(new TstNew<Word>());
	}
	
	@Test
	public void testIterator() {
		testIteratorImpl(new Ydict<Word>());
		testIteratorImpl(new TST<Word>());
		testIteratorImpl(new Trie<Word>());
		testIteratorImpl(new TstNew<Word>());
	}
	
	private void testTinyImpl(Dictionary<char[], Word> dict) {
		dict.insert("NEIGHBORHOOD".toCharArray(), new Word());
		dict.insert("NUTTY".toCharArray(), new Word());
		dict.insert("NUT".toCharArray(), new Word());
		dict.rebalance();
		System.out.println(dict.toString());
		assertTrue(dict.isPatternInDictionary("N__".toCharArray()));
	}

	private void testBasicOperationImpl(Dictionary<char[], Word> dict) {
		dict.insert("ALE".toCharArray(), new Word());
		dict.insert("DEF".toCharArray(), new Word());
		dict.insert("ALEX".toCharArray(), new Word());
		dict.insert("ZOO".toCharArray(), new Word());
		dict.insert("YAM".toCharArray(), new Word());
		dict.insert("MAN".toCharArray(), new Word());
		dict.insert("NEIGHBORHOOD".toCharArray(), new Word());
		dict.insert("NUTTY".toCharArray(), new Word());
		dict.insert("NUT".toCharArray(), new Word());
		dict.rebalance();
		assertNotNull(dict.lookup("ALE".toCharArray()));
		assertNotNull(dict.lookup("DEF".toCharArray()));
		assertNotNull(dict.lookup("ALEX".toCharArray()));
		assertTrue(dict.isPatternInDictionary("ALE".toCharArray()));
		assertTrue(dict.isPatternInDictionary("DEF".toCharArray()));
		assertTrue(dict.isPatternInDictionary("ALEX".toCharArray()));
//		dict.remove("ALE".toCharArray());
//		assertNull(dict.lookup("ALE".toCharArray()));
//		assertFalse(dict.isPatternInDictionary("ALE".toCharArray()));
//		assertTrue(dict.isPatternInDictionary("ALEX".toCharArray()));
		assertTrue(dict.isPatternInDictionary("A__".toCharArray()));
		assertTrue(dict.isPatternInDictionary("A___".toCharArray()));
		assertTrue(dict.isPatternInDictionary("D__".toCharArray()));
		assertTrue(dict.isPatternInDictionary("_E_".toCharArray()));
		assertTrue(dict.isPatternInDictionary("Z__".toCharArray()));
		assertTrue(dict.isPatternInDictionary("Y__".toCharArray()));
		assertTrue(dict.isPatternInDictionary("M__".toCharArray()));
		System.out.println(dict.toString());
		assertTrue(dict.isPatternInDictionary("N__".toCharArray()));
	}
	
	private void testIteratorImpl(Dictionary<char[], Word> dict) {
		String[] testWords = new String[] {"ALE", "DEF", "ALEX", "BENT", "BEN", "PEZ", "BAA", "HEN", "ABA", "APER", "APED", "APEX"};
		for (String testWord : testWords)
			dict.insert(testWord.toCharArray(), new Word());
		dict.rebalance();
		
		System.out.println("dict = " + dict.toString());
		Set<String> foundWords = getWordsFromIterator(dict.getIterator("_E_".toCharArray()));
		
		assertTrue(foundWords.size() == 4);
		assertTrue(foundWords.contains("DEF"));
		assertTrue(foundWords.contains("BEN"));
		assertTrue(foundWords.contains("PEZ"));
		assertTrue(foundWords.contains("HEN"));

		assertTrue(getWordsFromIterator(dict.getIterator("__Y".toCharArray())).isEmpty());
		
		ResettableIterator<Pair<char[], Word>> it = dict.getIterator("A_E_".toCharArray());
		foundWords = getWordsFromIterator(it);
		assertTrue(foundWords.size() == 4);
		assertTrue(foundWords.contains("ALEX"));
		assertTrue(foundWords.contains("APED"));
		assertTrue(foundWords.contains("APER"));
		assertTrue(foundWords.contains("APEX"));
		it.reset();
		Set<String> foundWords2 = getWordsFromIterator(it);
		assertTrue(foundWords2.equals(foundWords));
	}

	// return all words from the iterator, asserting that they each only appear once
	private Set<String> getWordsFromIterator(Iterator<Pair<char[], Word>> it) {
		Set<String> retval = new HashSet<String>();
		while (it.hasNext()) {
			String dictWord = String.valueOf(it.next().first);
			assertFalse(retval.contains(dictWord));
			retval.add(dictWord);
		}
		return retval;
	}
}
