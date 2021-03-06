/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Interface for objects which listen to changes to a single cell.
 * NOTE: Any listener that receives FullGridChange events should probably
 * also be prepared for CellChangeEvents, as generally either one or the other occurs.
 * @author Alex Stangl
 */
public interface CellChangeListener {

	/**
	 * Receive notification that grid cell has changed state in some way.
	 * The listener can respond in any way desired, updating UI
	 * components, etc.
	 * @param grid grid that has changed state
	 * @param cell cell that has changed state
	 * @param row row of cell that has changed state
	 * @param column column of cell that has changed state
	 */
	void handleChange(Grid grid, Cell cell, int row, int column);
}
