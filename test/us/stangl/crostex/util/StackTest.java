/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * JUnit tests for Stack.
 */
public class StackTest {

	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testEmptyStackPop() {
		Stack<String> stack = new Stack<String>();
		assertTrue(stack.empty());
		stack.pop();
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void testEmptyStackPeek() {
		Stack<String> stack = new Stack<String>();
		assertTrue(stack.empty());
		stack.peek();
	}

	@Test
	public void testPushPeekAndPop() {
		Stack<String> stack = new Stack<String>();
		assertTrue(stack.empty());
		String myString = "abc";
		stack.push(myString);
		
		assertFalse(stack.empty());
		
		assertEquals(myString, stack.peek());
		assertFalse(stack.empty());

		assertEquals(myString, stack.pop());
		assertTrue(stack.empty());
	}
}
