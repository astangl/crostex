/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

/**
 * Misc. static utility methods for manipulating Strings.
 * @author Alex Stangl
 */
public class StringUtils {

	/**
	 * Rotate specified string left n characters
	 * @param string string to rotate
	 * @param n number of positions to rotate string left
	 * @return specified string rotated left by n characters
	 * @throws StringIndexOutOfBoundsException if n negative or greater-than-the length of string
	 */
	public static String rotateLeft(String string, int n) {
		return new StringBuilder(string.length())
			.append(string.substring(n))
			.append(string.substring(0, n))
			.toString();
	}

	/**
	 * Rotate specified string right n characters
	 * @param string string to rotate
	 * @param n number of positions to rotate string right
	 * @return specified string rotated right by n characters
	 * @throws StringIndexOutOfBoundsException if n negative or greater-than-the length of string
	 */
	public static String rotateRight(String string, int n) {
		return rotateLeft(string, string.length() - n);
	}

}
