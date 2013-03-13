/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.command;

import us.stangl.crostex.Cell;
import us.stangl.crostex.Grid;
import us.stangl.crostex.util.RowColumnPair;

/**
 * Undoable command to enter a specified character into the current cell in the grid, and advance cursor.
 * @author Alex Stangl
 */
public class EnterCharacterToCellCommand implements UndoableCommand<Grid> {
	// row, column coordinates of cell to manipulate
	private final RowColumnPair coordinates;
	
	// original value of cell
	private final String oldContents;
	
	// new value of cell
	private final String newContents;
	
	// new value of (row, column) coordinates
	private final RowColumnPair newCoordinates;
	
	// whether cell change causes renumbering to occur (i.e., changing a black cell)
	private final boolean renumberRequired = false;
	
	public EnterCharacterToCellCommand(Grid grid, char c) {
		int oldCurrentRow = grid.getCurrentRow();
		int oldCurrentColumn = grid.getCurrentColumn();
		coordinates = new RowColumnPair(oldCurrentRow, oldCurrentColumn);
		Cell cell = grid.getCell(oldCurrentRow, oldCurrentColumn);
		oldContents = cell.getContents();
		newContents = String.valueOf(c);
		// renumberRequired = cell.isBlack();
		this.newCoordinates = grid.getNextCursorPosition();
	}
	/**
	 * Apply command to object.
	 * (Can't call it do since that's a reserved keyword.)
	 */
	public void apply(Grid grid) {
		int row = coordinates.row;
		int column = coordinates.column;
		Cell cell = grid.getCell(row, column);
		cell.setContents(newContents);
		grid.setCurrentRow(newCoordinates.row);
		grid.setCurrentColumn(newCoordinates.column);
		if (renumberRequired)
			grid.renumberCells();
		
		grid.notifyGridChangeListeners();
		if (renumberRequired)
			grid.notifyFullGridChangeListeners();
		else
			grid.notifyCellChangeListeners(cell, row, column);
	}
	
	/**
	 * Unapply (undo) command to object.
	 * Assumes object is in state just subsequent to command having been performed,
	 * or has been undone back to that same state.
	 */
	public void unApply(Grid grid) {
		int row = coordinates.row;
		int column = coordinates.column;
		Cell cell = grid.getCell(row, column);
		cell.setContents(oldContents);
		grid.setCurrentRow(row);
		grid.setCurrentColumn(column);
		if (renumberRequired)
			grid.renumberCells();
		
		grid.notifyGridChangeListeners();
		if (renumberRequired)
			grid.notifyFullGridChangeListeners();
		else
			grid.notifyCellChangeListeners(cell, row, column);
	}
}
