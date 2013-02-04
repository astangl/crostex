/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.util.Iterator;

/**
 * Iterator interface that supports a reset method to restore the iterator to its original state,
 * and method to allow iterator to jump ahead.
 */
public interface ResettableIterator<E> extends Iterator<E> {
	/**
	 * Restore iterator to its original, newly-created state
	 */
	void reset();
	
	/**
	 * Skip past the letter in the Nth position from left.
	 * E.g., if AGAR was just returned.
	 * NOTE: should call this BEFORE hasNext, as it is possible that this may change its return value
	 */
	void avoidLetterAt(int index);
}
