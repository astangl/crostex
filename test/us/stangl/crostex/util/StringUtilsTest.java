/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for StringUtils
 * @author Alex Stangl
 */
public class StringUtilsTest {

	@Test
	public void testRotateEmptyLeft() {
		assertEquals("", StringUtils.rotateLeft("", 0));
	}
	
	@Test
	public void testRotateEmptyRight() {
		assertEquals("", StringUtils.rotateRight("", 0));
	}
	
	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testNegativeRotateLeft() {
		StringUtils.rotateLeft("ABC", -1);
	}
	
	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testNegativeRotateRight() {
		StringUtils.rotateRight("ABCD", -1);
	}

	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testRotateLeftTooFar() {
		StringUtils.rotateLeft("ABC", 4);
	}
	
	@Test(expected=StringIndexOutOfBoundsException.class)
	public void testRotateRightTooFar() {
		StringUtils.rotateRight("ABC", 4);
	}

	@Test
	public void testRotateZeroLeft() {
		String testString = "mytest";
		assertEquals(testString, StringUtils.rotateLeft(testString, 0));
	}

	@Test
	public void testRotateZeroRight() {
		String testString = "mytest";
		assertEquals(testString, StringUtils.rotateRight(testString, 0));
	}
	@Test
	public void testRotateFullStringLengthLeft() {
		String testString = "mytest";
		assertEquals(testString, StringUtils.rotateLeft(testString, testString.length()));
	}

	@Test
	public void testRotateFullStringLengthRight() {
		String testString = "mytest";
		assertEquals(testString, StringUtils.rotateRight(testString, testString.length()));
	}
	
	@Test
	public void testRotatePartialLeft() {
		assertEquals("efabcd", StringUtils.rotateLeft("abcdef", 4));
	}

	@Test
	public void testRotatePartialRight() {
		assertEquals("cdefab", StringUtils.rotateRight("abcdef", 4));
	}
	
	@Test
	public void testSymmetryOnRotates() {
		String testString = "fancyTest";
		assertEquals(testString, StringUtils.rotateLeft(StringUtils.rotateRight(testString, 3), 3));
		assertEquals(testString, StringUtils.rotateRight(StringUtils.rotateLeft(testString, 3), 3));
	}
}
