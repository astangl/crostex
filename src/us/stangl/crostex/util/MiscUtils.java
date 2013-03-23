/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.ArrayList;

/**
 * Miscellaneous static utility methods.
 * @author Alex Stangl
 */
public class MiscUtils {

	/**
	 * Return ArrayList built of the specified elements. 
	 * @param elements elements to put into ArrayList
	 * @return ArrayList built of the specified elements
	 */
	public static <T> ArrayList<T> arrayList(T... elements) {
		ArrayList<T> retval = new ArrayList<T>(elements.length);
		for (T element : elements)
			retval.add(element);
		return retval;
	}
}
