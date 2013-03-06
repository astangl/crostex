/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.command;

import java.util.ArrayList;
import java.util.List;

import us.stangl.crostex.AcrossDownDirection;
import us.stangl.crostex.Cell;
import us.stangl.crostex.Grid;
import us.stangl.crostex.util.Pair;

/**
 * Undoable command to set to black current cell and possibly its symmetric twin
 * in the crossword puzzle.
 * @author Alex Stangl
 */
public class SetCurrentCellBlackCommand  implements UndoableCommand<Grid> {
	// list of (row, column) coordinates of cells to set to black
	private final List<Pair<Integer, Integer>> coordinates = new ArrayList<Pair<Integer, Integer>>(2);
	
	// list of values to apply to each corresponding cell
	private final List<Boolean> applyValues = new ArrayList<Boolean>(2);
	
	// list of values to unApply to each corresponding cell
	private final List<Boolean> unApplyValues = new ArrayList<Boolean>(2);

	// original currentRow
	private final int oldCurrentRow;
	
	// original currentColumn
	private final int oldCurrentColumn;
	
	// new value of currentRow
	private final int newCurrentRow;
	
	// new value of currentColumn
	private final int newCurrentColumn;
	
	public SetCurrentCellBlackCommand(Grid grid) {
		oldCurrentRow = grid.getCurrentRow();
		oldCurrentColumn = grid.getCurrentColumn();
		coordinates.add(new Pair<Integer, Integer>(Integer.valueOf(oldCurrentRow), Integer.valueOf(oldCurrentColumn)));
		Cell currentCell = grid.getCell(oldCurrentRow, oldCurrentColumn);
		boolean currentlyBlack = currentCell.isBlack();
		unApplyValues.add(Boolean.valueOf(currentlyBlack));
		applyValues.add(Boolean.TRUE);
		if (grid.isMaintainingSymmetry()) {
			int otherRow = grid.getHeight() - 1 - oldCurrentRow;
			int otherColumn = grid.getWidth() - 1 - oldCurrentColumn;
			coordinates.add(new Pair<Integer, Integer>(Integer.valueOf(otherRow), Integer.valueOf(otherColumn)));
			Cell otherCell = grid.getCell(otherRow, otherColumn);
			unApplyValues.add(Boolean.valueOf(otherCell.isBlack()));
			applyValues.add(Boolean.TRUE);
		}
		AcrossDownDirection currentDirection = grid.getCurrentDirection();
		int newCurrentRow = oldCurrentRow;
		int newCurrentColumn = oldCurrentColumn;
		if (currentDirection == AcrossDownDirection.DOWN && oldCurrentRow < grid.getHeight() - 1) {
			++newCurrentRow;
		} else if (currentDirection == AcrossDownDirection.ACROSS && oldCurrentColumn < grid.getWidth() - 1) {
			++newCurrentColumn;
		}
		this.newCurrentRow = newCurrentRow;
		this.newCurrentColumn = newCurrentColumn;
	}
	
	/**
	 * Apply command to object.
	 * (Can't call it do since that's a reserved keyword.)
	 */
	public void apply(Grid grid) {
		grid.setCurrentRow(newCurrentRow);
		grid.setCurrentColumn(newCurrentColumn);
		applySpecifiedValues(grid, applyValues);
	}
	
	/**
	 * Unapply (undo) command to object.
	 * Assumes object is in state just subsequent to command having been performed,
	 * or has been undone back to that same state.
	 */
	public void unApply(Grid grid) {
		grid.setCurrentRow(oldCurrentRow);
		grid.setCurrentColumn(oldCurrentColumn);
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
