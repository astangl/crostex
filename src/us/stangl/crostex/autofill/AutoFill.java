/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.autofill;

import us.stangl.crostex.Grid;
import us.stangl.crostex.Word;
import us.stangl.crostex.dictionary.Dictionary;

/**
 * Interface for classes providing grid autofill service.
 * @author Alex Stangl
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
