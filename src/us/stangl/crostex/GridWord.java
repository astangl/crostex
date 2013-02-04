/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Word in a Grid.
 */
public class GridWord {
	/** flag to enable additional debug code */
	private static final boolean DEBUG = false;
	
	/** number associated with word */
	private final int number_;
	
	public enum Direction { ACROSS, DOWN };
	
	/** direction of word */
	private final Direction direction_;
	
	/** Cells comprising word */
	private final Cell[] cells_;
	
	/** scratchpad to use for building pattern */
	private char[] patternScratchpad_ = new char[0];
	
	public GridWord(Cell[] cells, Direction direction, int number) {
		number_ = number;
		direction_ = direction;
		cells_ = cells;
	}
	
	public boolean isEligibleForAutofill() {
		for (Cell cell : cells_) {
			if (cell.isEligibleForAutofill())
				return true;
		}
		return false;
	}

	public int getNumberCellsRequiringAutofill() {
		int retval = 0;
		for (Cell cell : cells_)
			if (cell.isEligibleForAutofill())
				++retval;
		return retval;
	}

	/** return intersection cell, if any, otherwise null */
	public Cell getIntersection(GridWord otherWord) {
		for (Cell cell : cells_)
			if (otherWord.contains(cell))
				return cell;
		return null;
	}
	
	public boolean contains(Cell cell) {
		for (Cell myCell : cells_) {
			if (cell == myCell)
				return true;
		}
		return false;
	}
	
	/** whether it describes a complete word */
	public boolean isComplete() {
		for (Cell cell : cells_) {
			String cellContents = cell.getContents();
			if (cellContents == null || cellContents.length() == 0)
				return false;
		}
		return true;
	}

	/** return String described by GridWord */
	public String getContents() {
		StringBuilder retval = new StringBuilder();
		for (Cell cell : cells_) {
			String cellContents = cell.getContents();
			if (cellContents != null)
				retval.append(cellContents);
		}
		return retval.toString();
	}
	
	/** return zero-based index of Cell in this GridWord, or -1 if the cell is not in this GridWord */
	public int indexOf(Cell cell) {
		for (int i = 0; i < cells_.length; ++i)
			if (cells_[i] == cell)
				return i;
		return -1;
	}

//	/**
//	 * Attempt to set word based upon autofill.
//	 * If IllegalArgumentException thrown, partial failure can occur -- we don't attempt to rollback previous cells,
//	 * since this is considered to be strictly a programming error.
//	 * @throw IllegalArgumentException if trying to replace value of a non-autofill cell
//	 */ 
//	public void setAutofillContents(char[] contents) {
//		int len = cells.length;
//		for (int i = 0; i < len; ++i) {
//			cells[i].setContents(contents[indexes[i]]);
//		}
//	}
	
	/**
	 * Attempt to set word based upon autofill.
	 * If IllegalArgumentException thrown, partial failure can occur -- we don't attempt to rollback previous cells,
	 * since this is considered to be strictly a programming error.
	 * @throw IllegalArgumentException if trying to replace value of a non-autofill cell
	 */ 
	public void setAutofillContents(char[] contents, Pair<Cell[], int[]> fillConfig) {
		Cell[] cells = fillConfig.first_;
		int[] indexes = fillConfig.second_;
		int len = cells.length;
		for (int i = 0; i < len; ++i) {
			cells[i].setContents(contents[indexes[i]]);
		}
//		int index = 0;
//		for (Cell cell : cells_) {
//			if (cell.isEligibleForAutofill()) {
//				cell.setContents(contents[index]);
////				cell.setContents(contents.substring(index, index + 1));
//				++index;
//			} else {
//				int len = cell.getContentsSize();
//				if (DEBUG) {
//					if (! cell.confirmContents(contents, index, len)) {
//	//				String cellContents = cell.getContents();
//	//				if (! cellContents.equals(contents.substring(index, index + cellContents.length()))) {
//						throw new IllegalArgumentException("Cannot store contents " + contents
//								+ " into autofill word because string mismatch with cell contents "
//								+ " at index " + index);
//					}
//				}
////				index += cellContents.length();
//				index += len;
//			}
//		}
//		if (DEBUG) {
//			if (index != contents.length)
//				throw new IllegalArgumentException("Expected autofill contents " + contents + " to be of length " + index);
//		}
	}

	/** return contents as Map<Cell, String> */
	public Map<Cell, String> getContentsAsMap() {
		Map<Cell, String> retval = new HashMap<Cell, String>();
		for (Cell cell : cells_)
			retval.put(cell, cell.getContents());
		return retval;
	}

	/** return configuration of all cells in word which are eligible for autofill */
	public Pair<Cell[], int[]> getFillConfig() {
		int autoFillCellCount = 0;
		for (Cell cell : cells_)
			if (cell.isEligibleForAutofill())
				++autoFillCellCount;
		Cell[] autoFillCells = new Cell[autoFillCellCount];
		int[] indexes = new int[autoFillCellCount];
		int cellIndex = 0;
		int keyIndex = 0;
		for (Cell cell : cells_) {
			if (cell.isEligibleForAutofill()) {
				autoFillCells[cellIndex] = cell;
				indexes[cellIndex++] = keyIndex++;
			} else {
				keyIndex += cell.getContentsSize();
			}
		}
		return new Pair<Cell[], int[]>(autoFillCells, indexes);
	}

	/** restore word back to its original state, reflected in the saveConfig argument, from an earlier getFillConfig */
	public void restoreFromFillConfig(Pair<Cell[], int[]> saveConfig) {
		int len = saveConfig.first_.length;
		for (int i = 0; i < len; ++i)
			saveConfig.first_[i].setEmpty();
	}
	
	/** restore all cells from previously saved contents from getContentsAsMap */
	public void restoreFromMap(Map<Cell, String> oldValues) {
		for (Cell cell : cells_)
			cell.setContents(oldValues.get(cell));
	}

	/** return pattern described by this GridWord, possibly containing _ wildcards representing unknown/autofillable cells */
	public char[] getPattern() {
		int index = 0;
		for (Cell cell : cells_) {
			if (cell.isEligibleForAutofill()) {
				if (index >= patternScratchpad_.length)
					growPattern();
				patternScratchpad_[index++] = '_';
			} else {
//TODO copy cell contents directly into array efficiently
				int contentSize = cell.getContentsSize();
				int growthRequired = (index + contentSize - patternScratchpad_.length);
				if (growthRequired > 0)
					growPattern(growthRequired);
				cell.appendContents(patternScratchpad_, index);
				index += contentSize;
				//				String cellContents = cell.getContents();
//				if (cellContents != null)
//					retval.append(cellContents);
			}
		}
		if (DEBUG) {
			if (index != patternScratchpad_.length) {
				throw new RuntimeException("index value " + index + " not expected patternScratchpad size " + patternScratchpad_.length);
			}
		}
		return patternScratchpad_;
	}

	private void growPattern() {
		growPattern(1);
	}

	private void growPattern(int numElementsToAdd) {
		char[] newScratch = new char[patternScratchpad_.length + numElementsToAdd];
		for (int i = 0; i < patternScratchpad_.length; ++i)
			newScratch[i] = patternScratchpad_[i];
		patternScratchpad_ = newScratch;
	}

	/** return zero-based index of Cell in the pattern associated with this GridWord, or -1 if the cell is not associated with this GridWord */
	public int indexOfCellInPattern(Cell cell) {
		int retval = 0;
		for (Cell cellTemp : cells_) {
			if (cell == cellTemp)
				return retval;
			if (cellTemp.isEligibleForAutofill())
				++retval;
			else
				retval += cellTemp.getContents().length();
		}
		return -1;
	}


	/**
	 * @return the direction
	 */
	public Direction getDirection() {
		return direction_;
	}

	/**
	 * @return the number
	 */
	public int getNumber() {
		return number_;
	}
	
	public String toString() {
		StringBuilder retval = new StringBuilder();
		retval.append("{number_ = ")
			.append(number_)
			.append(", direction_ = ")
			.append(direction_)
			.append(", cells_ = [");
		for (int i = 0; i < cells_.length; ++i) {
			if (i > 0)
				retval.append(", ");
			retval.append(cells_[i]);
		}
		retval.append("]}");
		return retval.toString();
	}

	/**
	 * @return the cells
	 */
	public Cell[] getCells() {
		return cells_;
	}

}
