/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

/**
 * Interface for an object that can manufacture an instance of IoGrid.
 * @author Alex Stangl
 */
public interface IoGridFactory<T extends IoGrid> {
	/**
	 * Return new IoGrid instance with the specified width and height.
	 * @param width width of grid to return
	 * @param height height of grid to return
	 * @return new IoGrid instance with the specified width and height
	 */
	T newGrid(int width, int height);
}
