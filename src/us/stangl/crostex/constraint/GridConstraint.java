/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.constraint;

import us.stangl.crostex.Grid;

/**
 * Public interface for a constraint on a grid.
 * @author Alex Stangl
 */
public interface GridConstraint {
	/**
	 * @param grid
	 * @return whether this constraint is satisfied by the specified grid
	 */
	boolean satisfiedBy(Grid grid);
}
