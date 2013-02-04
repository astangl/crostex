/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.constraint;

import us.stangl.crostex.Grid;

/**
 * Grid constraint that enforces 180 degree rotational symmetry.
 */
public class SymmetryGridConstraint  implements GridConstraint {
	/**
	 * @return whether grid black pattern is identical when rotated 180 degrees
	 */
	public boolean satisfiedBy(Grid grid) {
		int lastRowToCheck = (grid.getHeight() - 1) / 2;
		int index = 0;
		for (int row = 0; row <= lastRowToCheck; ++row) {
			for (int col = 0; col < grid.getWidth(); ++col) {
				int mirror = calcMirrorCoord(grid, index);
				int mirrorRow = mirror / grid.getWidth();
				int mirrorCol = mirror % grid.getWidth();
				if (grid.getCell(row, col).isBlack() != grid.getCell(mirrorRow, mirrorCol).isBlack())
					return false;
				
				++index;
			}
		}
		return true;
	}
	
	/** given grid and coord (0 ... N * M) return its symmetric complement */
	private int calcMirrorCoord(Grid grid, int coord) {
		return grid.getHeight() * grid.getWidth() - coord - 1; 
	}
}
