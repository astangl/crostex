/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.command;

import java.util.ArrayList;
import java.util.List;

import us.stangl.crostex.Cell;
import us.stangl.crostex.Grid;
import us.stangl.crostex.util.Pair;

/**
 * Undoable command to toggle current cell and possibly its symmetric twin
 * in the crossword puzzle.
 * @author Alex Stangl
 */
public class ToggleCurrentCellCommand implements UndoableCommand<Grid> {
	// list of (row, column) coordinates of cells to toggle
	private final List<Pair<Integer, Integer>> coordinates = new ArrayList<Pair<Integer, Integer>>(2);
	
	// list of values to apply to each corresponding cell
	private final List<Boolean> applyValues = new ArrayList<Boolean>(2);
	
	// list of values to unApply to each corresponding cell
	private final List<Boolean> unApplyValues = new ArrayList<Boolean>(2);

	public ToggleCurrentCellCommand(Grid grid) {
		int currentRow = grid.getCurrentRow();
		int currentColumn = grid.getCurrentColumn();
		coordinates.add(new Pair<Integer, Integer>(Integer.valueOf(currentRow), Integer.valueOf(currentColumn)));
		Cell currentCell = grid.getCell(currentRow, currentColumn);
		boolean currentlyBlack = currentCell.isBlack();
		boolean nowBlack = ! currentlyBlack;
		unApplyValues.add(Boolean.valueOf(currentlyBlack));
		applyValues.add(Boolean.valueOf(nowBlack));
		if (grid.isMaintainingSymmetry()) {
			int otherRow = grid.getHeight() - 1 - currentRow;
			int otherColumn = grid.getWidth() - 1 - currentColumn;
			coordinates.add(new Pair<Integer, Integer>(Integer.valueOf(otherRow), Integer.valueOf(otherColumn)));
			Cell otherCell = grid.getCell(otherRow, otherColumn);
			unApplyValues.add(Boolean.valueOf(otherCell.isBlack()));
			applyValues.add(Boolean.valueOf(nowBlack));
		}
	}
	
	/**
	 * Apply command to object.
	 * (Can't call it do since that's a reserved keyword.)
	 */
	public void apply(Grid grid) {
		applySpecifiedValues(grid, applyValues);
	}
	
	/**
	 * Unapply (undo) command to object.
	 * Assumes object is in state just subsequent to command having been performed,
	 * or has been undone back to that same state.
	 */
	public void unApply(Grid grid) {
		applySpecifiedValues(grid, unApplyValues);
	}
	
	// apply specified set of values to the specified puzzle
	private void applySpecifiedValues(Grid grid, List<Boolean> values) {
		for (int i = 0; i < coordinates.size(); ++i) {
			Pair<Integer, Integer> rc = coordinates.get(i);
			grid.getCell(rc.first, rc.second).setBlack(values.get(i));
		}
		grid.renumberCells();
	}
}
