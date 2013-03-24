/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

/**
 * Unit tests for IdentityHashSet.
 * @author Alex Stangl
 */
public class IdentityHashSetTest {

	@Test
	public void testBasicOperation() {
		Set<String> set = new IdentityHashSet<String>();
		String a1 = new String("A");
		String a2 = new String("A");
		String a3 = new String("A");
		String a4 = new String("A");
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
		assertFalse(set.iterator().hasNext());
		set.add(a1);
		assertFalse(set.isEmpty());
		assertEquals(1, set.size());
		assertTrue(set.iterator().hasNext());
		assertTrue(set.contains(a1));
		assertFalse(set.contains(a2));
		
		set.add(a2);
		assertTrue(set.contains(a2));
		assertFalse(set.contains(a3));
		assertFalse(set.remove(a4));
		
		set.clear();
		assertTrue(set.isEmpty());
		assertEquals(0, set.size());
	}
}
