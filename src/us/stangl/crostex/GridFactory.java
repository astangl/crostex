/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import us.stangl.crostex.io.IoGridFactory;

/**
 * Factory to create Grid instances
 * @author Alex Stangl
 */
public class GridFactory implements IoGridFactory {

	@Override
	public Grid newGrid(int width, int height) {
		return new Grid(width, height);
	}
}
