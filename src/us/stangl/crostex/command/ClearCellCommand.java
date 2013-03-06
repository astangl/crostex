/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.command;

import us.stangl.crostex.AcrossDownDirection;
import us.stangl.crostex.Cell;
import us.stangl.crostex.Grid;
import us.stangl.crostex.NsewDirection;
import us.stangl.crostex.util.Pair;
import us.stangl.crostex.util.RowColumnPair;

/**
 * Undoable command to clear current cell in the grid, and advance cursor.
 * @author Alex Stangl
 */
public class ClearCellCommand implements UndoableCommand<Grid> {
	// row, column coordinates of cell to manipulate
	private Pair<Integer, Integer> coordinates;
	
	// original value of cell
	private final String oldContents;
	
	// original currentRow
	private final int oldCurrentRow;
	
	// original currentColumn
	private final int oldCurrentColumn;
	
	// new value of (row, column) coordinates
	private final RowColumnPair newCoordinates;
	
	public ClearCellCommand(Grid grid) {
		oldCurrentRow = grid.getCurrentRow();
		oldCurrentColumn = grid.getCurrentColumn();
		coordinates = new Pair<Integer, Integer>(Integer.valueOf(oldCurrentRow), Integer.valueOf(oldCurrentColumn));
		Cell cell = grid.getCell(oldCurrentRow, oldCurrentColumn);
		oldContents = cell.getContents();
		this.newCoordinates = grid.getNextCursorPosition();
	}

	/**
	 * Apply command to object.
	 * (Can't call it do since that's a reserved keyword.)
	 */
	public void apply(Grid grid) {
		grid.getCell(coordinates.first, coordinates.second).setContents("");
		grid.setCurrentRow(newCoordinates.row);
		grid.setCurrentColumn(newCoordinates.column);
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
