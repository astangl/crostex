/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Interface for classes providing grid autofill service.
 */
public interface AutoFill {
	/**
	 * Attempt to autofill grid.
	 * @param grid grid to fill
	 * @param dict dictionary to use
	 * @return true if successfully filled grid
	 */
	boolean autoFill(Grid grid, Dictionary<char[], Word> dict);
}
