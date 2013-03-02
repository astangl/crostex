/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Interface for objects which listen for changes to a Grid.
 * @author Alex Stangl
 */
public interface GridChangeListener {
	/**
	 * Receive notification that grid has changed state in some way.
	 * The listener can respond in any way desired, updating UI
	 * components, etc.
	 * @param grid grid that has changed state
	 */
	void handleChange(Grid grid);
}
