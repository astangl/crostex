/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Interface for objects which listen to changes to a cell.
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
