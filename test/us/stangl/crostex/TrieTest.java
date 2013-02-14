/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import us.stangl.crostex.dictionary.Dictionary;
import us.stangl.crostex.dictionary.TST;
import us.stangl.crostex.dictionary.Trie;
import us.stangl.crostex.dictionary.TstNew;
import us.stangl.crostex.dictionary.Xdict;
import us.stangl.crostex.util.ResettableIterator;

/**
 * JUnit tests for Trie.
 */
public class TrieTest {
	@Test
	public void testTiny() {
		testTinyImpl(new TST());
		testTinyImpl(new Trie());
		testTinyImpl(new Xdict());
		testTinyImpl(new TstNew());
	}
	
	@Test
	public void testBasicOperation() {
		testBasicOperationImpl(new TST());
		testBasicOperationImpl(new Trie());
		testBasicOperationImpl(new Xdict());
		testBasicOperationImpl(new TstNew());
	}
	
	@Test
	public void testIterator() {
//		testIteratorImpl(new Xdict());
		testIteratorImpl(new TST());
		testIteratorImpl(new Trie());
		testIteratorImpl(new TstNew());
	}
	
	private void testTinyImpl(Dictionary dict) {
		dict.insert("NEIGHBORHOOD".toCharArray(), new Word());
//		dict.insert("NUTTY".toCharArray(), new Word());
		dict.insert("NUT".toCharArray(), new Word());
		System.out.println(dict.toString());
		assertTrue(dict.isPatternInDictionary("N__".toCharArray()));
	}

	private void testBasicOperationImpl(Dictionary dict) {
		assertNull(dict.lookup("ALE".toCharArray()));
		assertNull(dict.lookup("DEF".toCharArray()));
		assertNull(dict.lookup("ALEX".toCharArray()));
		assertFalse(dict.isPatternInDictionary("ALE".toCharArray()));
		assertFalse(dict.isPatternInDictionary("DEF".toCharArray()));
		assertFalse(dict.isPatternInDictionary("ALEX".toCharArray()));
		dict.insert("ALE".toCharArray(), new Word());
		dict.insert("DEF".toCharArray(), new Word());
		dict.insert("ALEX".toCharArray(), new Word());
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
		dict.insert("ZOO".toCharArray(), new Word());
		assertTrue(dict.isPatternInDictionary("Z__".toCharArray()));
		dict.insert("YAM".toCharArray(), new Word());
		assertTrue(dict.isPatternInDictionary("Y__".toCharArray()));
		dict.insert("MAN".toCharArray(), new Word());
		assertTrue(dict.isPatternInDictionary("M__".toCharArray()));
		dict.insert("NEIGHBORHOOD".toCharArray(), new Word());
		dict.insert("NUTTY".toCharArray(), new Word());
		dict.insert("NUT".toCharArray(), new Word());
		System.out.println(dict.toString());
		assertTrue(dict.isPatternInDictionary("N__".toCharArray()));
	}
	
	private void testIteratorImpl(Dictionary dict) {
		dict.insert("ALE".toCharArray(), new Word());
		dict.insert("DEF".toCharArray(), new Word());
		dict.insert("ALEX".toCharArray(), new Word());
		dict.insert("BENT".toCharArray(), new Word());
		dict.insert("BEN".toCharArray(), new Word());
		dict.insert("PEZ".toCharArray(), new Word());
		dict.insert("BAA".toCharArray(), new Word());
		dict.insert("HEN".toCharArray(), new Word());
		dict.insert("ABA".toCharArray(), new Word());
		dict.insert("APER".toCharArray(), new Word());
		dict.insert("APED".toCharArray(), new Word());
		dict.insert("APEX".toCharArray(), new Word());
		
		System.out.println("dict = " + dict.toString());
		ResettableIterator<Pair<char[], Word>> it = dict.getIterator("_E_".toCharArray());
		assertTrue(it.hasNext());
//		System.out.println(new String(it.next().first_));
		assertTrue(Arrays.equals("BEN".toCharArray(), it.next().first_));
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		
//		System.out.println(new String(it.next().first_));
		assertTrue(Arrays.equals("DEF".toCharArray(), it.next().first_));
		assertTrue(it.hasNext());
		assertTrue(Arrays.equals("HEN".toCharArray(), it.next().first_));
		assertTrue(it.hasNext());
		assertTrue(Arrays.equals("PEZ".toCharArray(), it.next().first_));
		assertFalse(it.hasNext());
		
		it = dict.getIterator("__Y".toCharArray());
		assertFalse(it.hasNext());
		
		it = dict.getIterator("A_E_".toCharArray());
		assertTrue(it.hasNext());
		assertTrue(Arrays.equals("ALEX".toCharArray(), it.next().first_));
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		//System.out.println(new String(it.next()));
		assertTrue(Arrays.equals("APED".toCharArray(), it.next().first_));
		assertTrue(it.hasNext());
//System.out.println(new String(it.next().first_));
		assertTrue(Arrays.equals("APER".toCharArray(), it.next().first_));
		assertTrue(it.hasNext());
		assertTrue(Arrays.equals("APEX".toCharArray(), it.next().first_));
//		System.out.println(new String(it.next().first_));
		while (it.hasNext()) {
			System.out.println("Bonus words! " + new String(it.next().first_));
		}
		assertFalse(it.hasNext());
		
	}
}
