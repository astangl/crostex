/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * JUnit tests. for CircularList.
 */
public class CircularListTest {
	@Test()
	public void testCircularListOps() {
		CircularList<String> stringList = new CircularList<String>();
		assertEquals(0, stringList.size());
		
		stringList.add("A");
		assertEquals(1, stringList.size());
		assertEquals("A", stringList.getNext());
		assertEquals("A", stringList.getNext());
		assertEquals("A", stringList.getNext());
		
		stringList.add("B");
		assertEquals(2, stringList.size());
		assertEquals("A", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("A", stringList.getNext());
		assertEquals("B", stringList.getNext());
		
		stringList.add("C");
		assertEquals(3, stringList.size());
		assertEquals("A", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("C", stringList.getNext());
		assertEquals("A", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("C", stringList.getNext());
		
		stringList.deletePrev();
		assertEquals(2, stringList.size());
		assertEquals("A", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("A", stringList.getNext());
		assertEquals("B", stringList.getNext());

		List<String> list = new ArrayList<String>();
		list.add("D");
		list.add("E");
		list.add("F");
		list.add("G");
		
		stringList.addAll(list);
		assertEquals(6, stringList.size());
		assertEquals("A", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("D", stringList.getNext());
		assertEquals("E", stringList.getNext());
		assertEquals("F", stringList.getNext());
		assertEquals("G", stringList.getNext());
		assertEquals("A", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("D", stringList.getNext());
		assertEquals("E", stringList.getNext());
		assertEquals("F", stringList.getNext());
		assertEquals("G", stringList.getNext());
		assertEquals("A", stringList.getNext());

		stringList.deletePrev();
		assertEquals("B", stringList.getNext());
		assertEquals("D", stringList.getNext());
		assertEquals("E", stringList.getNext());
		assertEquals("F", stringList.getNext());
		assertEquals("G", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("D", stringList.getNext());
		assertEquals("E", stringList.getNext());
		assertEquals("F", stringList.getNext());
		assertEquals("G", stringList.getNext());
		
		stringList.deletePrev();
		assertEquals("B", stringList.getNext());
		assertEquals("D", stringList.getNext());
		assertEquals("E", stringList.getNext());
		assertEquals("F", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("D", stringList.getNext());
		assertEquals("E", stringList.getNext());
		assertEquals("F", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("D", stringList.getNext());
		assertEquals("E", stringList.getNext());
		assertEquals(4, stringList.size());

		stringList.deletePrev();
		stringList.deletePrev();
		assertEquals("F", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("F", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("F", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("F", stringList.getNext());
		stringList.deletePrev();
		assertEquals("B", stringList.getNext());
		assertEquals("B", stringList.getNext());
		assertEquals("B", stringList.getNext());
		stringList.deletePrev();
		assertEquals(0, stringList.size());

	}
	

}
