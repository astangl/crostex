/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import us.stangl.crostex.constraint.GridConstraint;
import us.stangl.crostex.constraint.Min3LetterWordGridConstraint;
import us.stangl.crostex.constraint.OnePolyominoGridConstraint;

/**
 * Generate all valid grids of the specified size.
 */
public class GridGenerator {
	private final float MIN_PROPORTION_BLACK = 0.04f;		// minimum proportion of allowable black 0-1
	private final float MAX_PROPORTION_BLACK = 0.66f;		// maximum proportion of allowable black 0-1
	private final int height_;
	private final int width_;
	private final int minNumberBlack_;
	private final int maxNumberBlack_;
	private final boolean[] crosswordState_;
	private int blackCount_ = 0;
	
	/** flag indicating whether there is an odd number of cells (and therefore overlap in crosswordState_ symmetry) */
	private boolean oddNumberCells_;
	
	/** one polyomino grid constraint */
	private GridConstraint ONE_POLYOMINO_GRID_CONSTRAINT = new OnePolyominoGridConstraint();
	
	/** minimum 3-letter word grid constraint */
	private GridConstraint THREE_LETTER_WORD_GRID_CONSTRAINT = new Min3LetterWordGridConstraint();
	
	public GridGenerator(int height, int width) {
		height_ = height;
		width_ = width;
		int numCells = height_ * width_;
		int numCellsToVary = (numCells + 1) / 2;		// halve # of cells due to rotational symmetry
		crosswordState_ = new boolean[numCellsToVary];
		minNumberBlack_ = (int)(MIN_PROPORTION_BLACK * numCells + 0.5);
		maxNumberBlack_ = (int)(MAX_PROPORTION_BLACK * numCells + 0.5);
		oddNumberCells_ = (numCells % 2) != 0;
	}
	
	public void generateAll() {
		
		// Generate all numbers from 0 to 2 ** numCellsToVary - 1
		// Each number's binary representation represents a single possible crossword, with each 1/true representing a black square
		// To each, check additional constraints, such as single polyomino and minimum word length.

		int counter = 0;
		while (true) {
			Grid grid = new Grid(width_, height_, "", "");
			int lastCellIndex = width_ * height_ - 1;
			int blackCount = 0;
			for (int row = 0; row < height_; ++row)
				for (int col = 0; col < width_; ++col) {
					int index = row * width_ + col;
					if (index >= crosswordState_.length) {
						// accommodate lower half of grid by mirroring upper half
						index = lastCellIndex - index;
					}
					if (crosswordState_[index]) {
						grid.getCell(row, col).setBlack(true);
						++blackCount;
					}
				}
			
			if (ONE_POLYOMINO_GRID_CONSTRAINT.satisfiedBy(grid)
					&& THREE_LETTER_WORD_GRID_CONSTRAINT.satisfiedBy(grid)) {
				counter++;
//				System.out.println("Found grid!");
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
		} while (blackCount_ < minNumberBlack_ || blackCount_ > maxNumberBlack_);
		return true;
	}
	
	/** increment crossword puzzle state. Return true unless rolling over back to zero */
	private boolean incrementState() {
		boolean retval = true;
		int lastElementIndex = crosswordState_.length - 1;
		// start at last element and scan towards start, looking for false, flipping all trues to false...
		int index = lastElementIndex;
		while (index >= 0 && crosswordState_[index]) {
			--blackCount_;
			// Decrement black count again for lower half of puzzle unless on middle square which isn't duplicated
			if (index < lastElementIndex || !oddNumberCells_)
				--blackCount_;
			crosswordState_[index--] = false;
		}
			
		// If index < 0, no 0 digits found, so we are wrapping around, and no 1 is necessary
		if (index >= 0) {
			crosswordState_[index] = true;
			++blackCount_;
			// Increment black count again for lower half of puzzle unless on middle square which isn't duplicated
			if (index < lastElementIndex || !oddNumberCells_)
				++blackCount_;
		} else {
			retval = false;
		}
		
//		// ... Now change everything after index to false
//		while (++index <= lastElementIndex) {
//			crosswordState_[index] = false;
//			--blackCount_;
//			// Decrement black count again for lower half of puzzle unless on middle square which isn't duplicated
//			if (index < lastElementIndex || !oddNumberCells_)
//				--blackCount_;
//		}
		return retval;
	}

	public static void main(String args[]) {
		GridGenerator instance = new GridGenerator(7,7);
		instance.generateAll();
	}
}
