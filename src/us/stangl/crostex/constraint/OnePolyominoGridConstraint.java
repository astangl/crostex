/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.constraint;

import us.stangl.crostex.Grid;

/**
 * Constraint enforcing that a grid contain only 1 polyomino.
 * Polyomino is defined to be areas of adjacent (row or column adjacent, not diagonal) non-black cells.
 * 
 * American crosswords traditionally have this one polyomino constraint -- no isolated islands.
 * @author Alex Stangl
 */
public class OnePolyominoGridConstraint implements GridConstraint {
	/**
	 * @return whether grid has exactly one polyomino
	 */
	public boolean satisfiedBy(Grid grid) {
		// Create temp grid isomorphic (same-sized) to grid w/ -1 in blacks, 0 elsewhere
		int[][] tempGrid = new int[grid.getHeight()][grid.getWidth()];
		for (int row = 0; row < grid.getHeight(); ++row) {
			for (int col = 0; col < grid.getWidth(); ++col) {
				tempGrid[row][col] = grid.getCell(row, col).isBlack() ? -1 : 0;
			}
		}
		int firstEmptyCell = findValInGrid(tempGrid, 0);
		if (firstEmptyCell == -1)
			return false;				// all black grid! This degenerate case represents ZERO polyominos.

		floodFill(tempGrid, firstEmptyCell / tempGrid.length, firstEmptyCell % tempGrid.length, 1);
		return findValInGrid(tempGrid, 0) == -1;
	}
	
	// search for value in grid, returning its cell-number, where [0][0] is 0, and numbered across cols, then down rows (row-major), or -1 if not found
	private int findValInGrid(int[][] grid, int val) {
		int retval = 0;
		for (int row = 0; row < grid.length; ++row) {
			for (int col = 0; col < grid[row].length; ++col) {
				if (grid[row][col] == val)
					return retval;
				++retval;
			}
		}
		return -1;
	}
	
	// If row, col point to empty (0) cell in grid, set it to specified val and carry over to adjacent cells.
	private void floodFill(int[][] grid, int row, int col, int val) {
		if (row >= 0 && col >= 0 && row < grid.length && col < grid[0].length && grid[row][col] == 0) {
			grid[row][col] = val;
			floodFill(grid, row, col - 1, val);
			floodFill(grid, row, col + 1, val);
			floodFill(grid, row - 1, col, val);
			floodFill(grid, row + 1, col, val);
		}
	}
}
