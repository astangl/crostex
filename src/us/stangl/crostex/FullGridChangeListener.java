/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Interface for objects which listen to changes to the full grid.
 * This is used instead of CellChangeListener when bulk changes are
 * made to a grid. GridChangeListener is used when *any* changes are
 * made to a grid.
 * NOTE: Any listener that receives FullGridChange events should probably
 * also be prepared for CellChangeEvents, as generally either one or the other occurs.
 * @author Alex Stangl
 */
public interface FullGridChangeListener {
	/**
	 * Receive notification that grid has been substantially altered.
	 * The listener can respond in any way desired, updating UI
	 * components, etc.
	 * @param grid grid that has changed state
	 */
	void handleFullGridChange(Grid grid);
}
