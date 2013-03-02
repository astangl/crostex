/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.command;

import us.stangl.crostex.Cell;
import us.stangl.crostex.Grid;
import us.stangl.crostex.NsewDirection;
import us.stangl.crostex.util.Pair;

/**
 * Undoable command to enter a specified character into the current cell in the crossword puzzle.
 * @author Alex Stangl
 */
public class EnterCharacterToCellCommand implements UndoableCommand<Grid> {
	// row, column coordinates of cell to manipulate
	private Pair<Integer, Integer> coordinates;
	
	// original value of cell
	private final String oldContents;
	
	// new value of cell
	private final String newContents;
	
	// original currentRow
	private final int oldCurrentRow;
	
	// original currentColumn
	private final int oldCurrentColumn;
	
	// new value of currentRow
	private final int newCurrentRow;
	
	// new value of currentColumn
	private final int newCurrentColumn;
	
	public EnterCharacterToCellCommand(Grid grid, char c) {
		oldCurrentRow = grid.getCurrentRow();
		oldCurrentColumn = grid.getCurrentColumn();
		coordinates = new Pair<Integer, Integer>(Integer.valueOf(oldCurrentRow), Integer.valueOf(oldCurrentColumn));
		Cell cell = grid.getCell(oldCurrentRow, oldCurrentColumn);
		oldContents = cell.getContents();
		newContents = String.valueOf(c);
		NsewDirection currentDirection = grid.getCurrentDirection();
		int newCurrentRow = oldCurrentRow;
		int newCurrentColumn = oldCurrentColumn;
		if (currentDirection == NsewDirection.NORTH && oldCurrentRow > 0) {
			--newCurrentRow;
		} else if (currentDirection == NsewDirection.SOUTH && oldCurrentRow < grid.getHeight() - 1) {
			++newCurrentRow;
		} else if (currentDirection == NsewDirection.EAST && oldCurrentColumn < grid.getWidth() - 1) {
			++newCurrentColumn;
		} else if (currentDirection == NsewDirection.WEST && oldCurrentColumn > 0) {
			--newCurrentColumn;
		}
		this.newCurrentRow = newCurrentRow;
		this.newCurrentColumn = newCurrentColumn;
	}
	/**
	 * Apply command to object.
	 * (Can't call it do since that's a reserved keyword.)
	 */
	public void apply(Grid grid) {
		grid.getCell(coordinates.first, coordinates.second).setContents(newContents);
		grid.setCurrentRow(newCurrentRow);
		grid.setCurrentColumn(newCurrentColumn);
		grid.renumberCells();
	}
	
	/**
	 * Unapply (undo) command to object.
	 * Assumes object is in state just subsequent to command having been performed,
	 * or has been undone back to that same state.
	 */
	public void unApply(Grid grid) {
		grid.getCell(coordinates.first, coordinates.second).setContents(oldContents);
		grid.setCurrentRow(oldCurrentRow);
		grid.setCurrentColumn(oldCurrentColumn);
		grid.renumberCells();
	}
}
