/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.command;

import us.stangl.crostex.Cell;
import us.stangl.crostex.Grid;
import us.stangl.crostex.util.RowColumnPair;

/**
 * Undoable command to toggle current cell circled.
 * @author Alex Stangl
 */
public class ToggleCurrentCellCircledCommand implements UndoableCommand<Grid> {
	// (row, column) coordinate of cell to toggle
	private final RowColumnPair coordinate;
	
	// value to apply to the cell
	private final boolean applyValue;
	
	// value to unApply to the cell
	private final boolean unApplyValue;

	public ToggleCurrentCellCircledCommand(Grid grid) {
		int currentRow = grid.getCurrentRow();
		int currentColumn = grid.getCurrentColumn();
		coordinate = new RowColumnPair(currentRow, currentColumn);
		Cell currentCell = grid.getCell(currentRow, currentColumn);
		boolean currentlyCircled = currentCell.isCircled();
		boolean nowCircled = ! currentlyCircled;
		unApplyValue = currentlyCircled;
		applyValue = nowCircled;
	}
	
	/**
	 * Apply command to object.
	 * (Can't call it do since that's a reserved keyword.)
	 */
	public void apply(Grid grid) {
		applySpecifiedValues(grid, applyValue);
	}
	
	/**
	 * Unapply (undo) command to object.
	 * Assumes object is in state just subsequent to command having been performed,
	 * or has been undone back to that same state.
	 */
	public void unApply(Grid grid) {
		applySpecifiedValues(grid, unApplyValue);
	}
	
	// apply specified set of values to the specified puzzle
	private void applySpecifiedValues(Grid grid, boolean value) {
		RowColumnPair rc = coordinate;
		grid.getCell(rc.row, rc.column).setCircled(value);
		grid.notifyGridChangeListeners();
		grid.notifyFullGridChangeListeners();
	}
}
