/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.autofill;

import us.stangl.crostex.Grid;
import us.stangl.crostex.Word;
import us.stangl.crostex.dictionary.Dictionary;

/**
 * Interface for grid region auto-filler.
 * @author Alex Stangl
 */
public interface AutoFillRegion {
	/**
	 * Attempt to autofill grid region, from current grid position.
	 * Region is defined as all empty squares contiguous to the current position.
	 * If the current position is not an empty square, this is considered a no-op.
	 * @param grid grid to fill
	 * @param dict dictionary to use
	 * @return true if successfully filled grid
	 */
	boolean autoFillRegion(Grid grid, Dictionary<char[], Word> dict);
}
