/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import us.stangl.crostex.Grid;
import us.stangl.crostex.util.Message;

/**
 * Table model for Grid Picker
 */
public class GridPickerTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	/** full collection of underlying grids, of all sizes */
	private final Collection<Grid> grids;

	/** column names */
	private final String[] COLUMN_NAMES = new String[] {Message.TEXT_NAME.toString(), Message.TEXT_DESCRIPTION.toString(),
			Message.TEXT_NUMBER_OF_WORDS.toString(), Message.TEXT_PERCENT_BLACK.toString()};
	
	/**
	 * subset of full grid collection containing only grids of
	 * currently specified size. A dummy empty grid of specified
	 * size is always also included, so there will be at least one
	 * row in the table, even if the full collection has none of the
	 * specified size.
	 */ 
	private List<Grid> eligibleGrids = new ArrayList<Grid>();
	
	public GridPickerTableModel(Collection<Grid> grids) {
		this.grids = grids;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 4;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return eligibleGrids.size();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		return COLUMN_NAMES[col];
	}

	public Object getValueAt(int row, int col) {
		if (col >= getColumnCount())
			throw new IllegalArgumentException("Invalid col value: " + col);
		if (row >= eligibleGrids.size())
			throw new IllegalArgumentException("Invalid row value: " + row + ". Only " + eligibleGrids.size() + " rows available.");
		Grid grid = eligibleGrids.get(row);
		if (col == 0)
			return grid.getName();
		if (col == 1)
			return grid.getDescription();
		if (col == 2)
			return grid.getNumberOfWords();
		return Integer.toString(grid.getNumberBlackCells() * 100 / grid.getHeight() / grid.getWidth());
	}
	
	public Grid getGridAtRow(int row) {
		return eligibleGrids.get(row);
	}
	
	public void setGridDimensions(int width, int height) {
		eligibleGrids.clear();
		boolean emptyGridIncluded = false;
		for (Grid grid : grids) {
			if (grid.getHeight() == height && grid.getWidth() == width) {
				eligibleGrids.add(grid);
				if (grid.getNumberBlackCells() == 0)
					emptyGridIncluded = true;
			}
		}
		if (! emptyGridIncluded) {
			// Must include an extra empty grid
			String name = MessageFormat.format(Message.DEFAULT_GRID_NAME.toString(), width, height);
			String description = MessageFormat.format(Message.DEFAULT_GRID_DESCRIPTION.toString(), width, height);
			eligibleGrids.add(new Grid(width, height, name, description));
		}
		
		// Notify JTable that its data has changed
		fireTableDataChanged();
	}
}
