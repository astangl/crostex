/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for MiscUtils.
 * @author Alex Stangl
 */
public class MiscUtilsTest {

	@Test
	public void testArrayList() {
		List<String> stringList = new ArrayList<String>(3);
		stringList.add("A");
		stringList.add("B");
		stringList.add("C");
		assertEquals(stringList, MiscUtils.arrayList("A", "B", "C"));
	}
	
	@Test
	public void testEmptyArrayList() {
		assertTrue(MiscUtils.arrayList().isEmpty());
	}
}
