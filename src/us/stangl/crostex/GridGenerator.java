/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import us.stangl.crostex.constraint.GridConstraint;
import us.stangl.crostex.constraint.Min3LetterWordGridConstraint;
import us.stangl.crostex.constraint.OnePolyominoGridConstraint;

/**
 * Generate all valid grids of the specified size.
 * @author Alex Stangl
 */
public class GridGenerator {
	private final float MIN_PROPORTION_BLACK = 0.04f;		// minimum proportion of allowable black 0-1
	private final float MAX_PROPORTION_BLACK = 0.66f;		// maximum proportion of allowable black 0-1
	private final int height;
	private final int width;
	private final int minNumberBlack;
	private final int maxNumberBlack;
	
	// grid cell flags, true = black cell, false = non-black (length = half the grid (or half-1 if odd # cells) due to symmetry)
	private final boolean[] crosswordState;
	private int blackCount = 0;
	
	// whether there is an odd number of cells (and therefore overlap in crosswordState symmetry)
	private boolean oddNumberCells;
	
	// one polyomino grid constraint, to enforce there are no unconnected regions
	private GridConstraint ONE_POLYOMINO_GRID_CONSTRAINT = new OnePolyominoGridConstraint();
	
	// minimum 3-letter word grid constraint, to enforce no two-letter words
	private GridConstraint THREE_LETTER_WORD_GRID_CONSTRAINT = new Min3LetterWordGridConstraint();
	
	public GridGenerator(int height, int width) {
		this.height = height;
		this.width = width;
		int numCells = this.height * this.width;
		int numCellsToVary = (numCells + 1) / 2;		// halve # of cells due to rotational symmetry
		crosswordState = new boolean[numCellsToVary];
		minNumberBlack = (int)(MIN_PROPORTION_BLACK * numCells + 0.5);
		maxNumberBlack = (int)(MAX_PROPORTION_BLACK * numCells + 0.5);
		oddNumberCells = (numCells % 2) != 0;
	}
	
	public void generateAll() {
		
		// Generate all numbers from 0 to 2 ** numCellsToVary - 1
		// Each number's binary representation represents a single possible crossword, with each 1/true representing a black square
		// To each, check additional constraints, such as single polyomino and minimum word length.

		int counter = 0;
		while (true) {
			Grid grid = new Grid(width, height, "", "");
			int lastCellIndex = width * height - 1;
			int blackCount = 0;
			for (int row = 0; row < height; ++row)
				for (int col = 0; col < width; ++col) {
					int index = row * width + col;
					if (index >= crosswordState.length) {
						// accommodate lower half of grid by mirroring upper half
						index = lastCellIndex - index;
					}
					if (crosswordState[index]) {
						grid.getCell(row, col).setBlack(true);
						++blackCount;
					}
				}
			
			if (ONE_POLYOMINO_GRID_CONSTRAINT.satisfiedBy(grid)
					&& THREE_LETTER_WORD_GRID_CONSTRAINT.satisfiedBy(grid)) {
				counter++;
			}
			if (! incrementToNextPossiblyGoodState())
				break;
		}
		
		System.out.println("Found a total of " + counter + " grid templates!");
	}
	
	/**
	 *  increment crossword puzzle state, skipping clearly invalid states (i.e., words shorter than 3 cells).
	 *  @return true unless rolling over back to zero
	 */
	private boolean incrementToNextPossiblyGoodState() {
		do {
			boolean retval = incrementState();
			if (! retval)
				return retval;
		} while (blackCount < minNumberBlack || blackCount > maxNumberBlack);
		return true;
	}
	
	// increment crossword puzzle state. Return true unless rolling over back to zero
	private boolean incrementState() {
		boolean retval = true;
		int lastElementIndex = crosswordState.length - 1;
		
		// start at last element and scan towards start, looking for false, flipping all trues to false...
		int index;
		for (index = lastElementIndex; index >= 0 && crosswordState[index]; --index) {
			--blackCount;
			// Decrement black count again for lower half of puzzle unless on middle square which isn't duplicated
			if (index < lastElementIndex || !oddNumberCells)
				--blackCount;
			crosswordState[index] = false;
		}
			
		// If index < 0, no 0 digits found, so we are wrapping around, and no 1 is necessary
		if (index >= 0) {
			crosswordState[index] = true;
			++blackCount;
			// Increment black count again for lower half of puzzle unless on middle square which isn't duplicated
			if (index < lastElementIndex || !oddNumberCells)
				++blackCount;
		} else {
			retval = false;
		}
		
		return retval;
	}

	public static void main(String args[]) {
		GridGenerator instance = new GridGenerator(7,7);
		instance.generateAll();
	}
}
