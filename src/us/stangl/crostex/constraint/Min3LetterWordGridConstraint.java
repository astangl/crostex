/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.constraint;

import us.stangl.crostex.Grid;

/**
 * Grid constraint which enforces that all words in grid consist of at least 3 grid cells.
 */
public class Min3LetterWordGridConstraint implements GridConstraint {
	/**
	 * @return whether all words in grid consist of at least 3 grid cells
	 */
	public boolean satisfiedBy(Grid grid) {
		for (int row = 0; row < grid.getHeight(); ++row)
			for (int col = 0; col < grid.getWidth(); ++col) {
				if (grid.isStartOfAcrossWord(row, col)) {
					if (col + 2 >= grid.getWidth() || grid.getCell(row, col + 1).isBlack() || grid.getCell(row, col + 2).isBlack())
						return false;
				}
				if (grid.isStartOfDownWord(row, col))
				{
					if (row + 2 >= grid.getHeight() || grid.getCell(row + 1, col).isBlack() || grid.getCell(row + 2, col).isBlack())
						return false;
				}
			}
		return true;
	}
}
