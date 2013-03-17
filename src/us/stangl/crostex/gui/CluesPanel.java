/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import us.stangl.crostex.Cell;
import us.stangl.crostex.CellChangeListener;
import us.stangl.crostex.Clue;
import us.stangl.crostex.FullGridChangeListener;
import us.stangl.crostex.Grid;
import us.stangl.crostex.util.Message;
import us.stangl.crostex.util.RowColumnPair;

/**
 * Panel to display clues on a side tab.
 * @author Alex Stangl
 */
public final class CluesPanel extends JPanel implements FullGridChangeListener, CellChangeListener {
	private static final long serialVersionUID = 1L;
	
	// length to use for text fields
	private static final int TEXT_FIELD_LENGTH = 40;
	
	// associated grid
	private final Grid grid;
	
	private JScrollPane scrollPane;

	private SquareClueAssociation[][] squaresToClues;
	
	public CluesPanel(Grid grid) {
		this.grid = grid;
		
		grid.addFullGridChangeListener(this);
		grid.addCellChangeListener(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		regenerate();
	}
	
	public void regenerate() {
		// setup 2-dimensional array for looking up by grid coordinate later
		int nRows = grid.getHeight();
		int nCols = grid.getWidth();
		squaresToClues = new SquareClueAssociation[nRows][];
		for (int gridRow = 0; gridRow < nRows; ++gridRow) {
			squaresToClues[gridRow] = new SquareClueAssociation[nCols];
			for (int gridCol = 0; gridCol < nCols; ++gridCol) {
				squaresToClues[gridRow][gridCol] = new SquareClueAssociation();
			}
		}
		
		removeAll();
		GridBagLayout gbl = new GridBagLayout();
		JPanel topPanel = new JPanel();
		topPanel.setLayout(gbl);
		scrollPane = new JScrollPane(topPanel);
		
		int row = 0;
		topPanel.add(new JLabel(Message.LABEL_ACROSS.toString()), new GBC(0, row).anchor(GBC.NORTHWEST));
		topPanel.add(new JLabel(), new GBC(1, row).weightx(1.0).gridwidth(GBC.REMAINDER));
		++row;
		List<Clue> acrossClues = grid.validateAndGetAcrossClues();
		for (final Clue clue : acrossClues) {
			JLabel label = new JLabel(buildLabelString(clue));
			topPanel.add(label, new GBC(0, row).anchor(GBC.NORTHWEST));
			final JTextField textField = new JTextField(clue.getClueText(), TEXT_FIELD_LENGTH);
			boolean editable = clue.isWordComplete();
			if (editable) {
				textField.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						clue.setClueText(textField.getText());
					}
				});
				GuiUtils.addListenerToCommitOnFocusLost(textField);
			}
			textField.setEnabled(editable);
			textField.setEditable(editable);
			topPanel.add(textField, new GBC(1, row).anchor(GBC.NORTHWEST).weightx(1.0).gridwidth(GBC.REMAINDER));
			++row;
			
			RowColumnPair startOfWord = clue.getStartOfWord();
			RowColumnPair endOfWord = clue.getEndOfWord();
			int gridRow = startOfWord.row;
			for (int gridCol = startOfWord.column; gridCol <= endOfWord.column; ++gridCol) {
				SquareClueAssociation sca = squaresToClues[gridRow][gridCol];
				sca.acrossLabel = label;
				sca.acrossClueField = textField;
				sca.acrossCells = clue.getCells();
				sca.acrossClueNumber = clue.getNumber();
			}
		}
		topPanel.add(new JLabel(Message.LABEL_DOWN.toString()), new GBC(0, row).anchor(GBC.NORTHWEST));
		++row;
		List<Clue> downClues = grid.validateAndGetDownClues();
		for (final Clue clue : downClues) {
			JLabel label = new JLabel(buildLabelString(clue));
			topPanel.add(label, new GBC(0, row).anchor(GBC.NORTHWEST));
			final JTextField textField = new JTextField(clue.getClueText(), TEXT_FIELD_LENGTH);
			boolean editable = clue.isWordComplete();
			textField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					clue.setClueText(textField.getText());
				}
			});
			GuiUtils.addListenerToCommitOnFocusLost(textField);
			textField.setEnabled(editable);
			textField.setEditable(editable);
			topPanel.add(textField, new GBC(1, row).anchor(GBC.NORTHWEST).weightx(1.0).gridwidth(GBC.REMAINDER));
			++row;
			
			RowColumnPair startOfWord = clue.getStartOfWord();
			RowColumnPair endOfWord = clue.getEndOfWord();
			int gridCol = startOfWord.column;
			for (int gridRow = startOfWord.row; gridRow <= endOfWord.row; ++gridRow) {
				SquareClueAssociation sca = squaresToClues[gridRow][gridCol];
				sca.downLabel = label;
				sca.downClueField = textField;
				sca.downCells = clue.getCells();
				sca.downClueNumber = clue.getNumber();
			}
		}
		
		add(scrollPane);
	}
	
	/* (non-Javadoc)
	 * @see us.stangl.crostex.CellChangeListener#handleChange(us.stangl.crostex.Grid, us.stangl.crostex.Cell, int, int)
	 */
	public void handleChange(Grid grid, Cell cell, int row, int column) {
		SquareClueAssociation sca = squaresToClues[row][column];
		sca.acrossLabel.setText(buildLabelString(sca.acrossClueNumber, sca.acrossCells));
		sca.downLabel.setText(buildLabelString(sca.downClueNumber, sca.downCells));

		enableOrDisableField(sca.acrossClueField, allCellsFilled(sca.acrossCells));
		enableOrDisableField(sca.downClueField, allCellsFilled(sca.downCells));
	}
	
	@Override
	public void handleFullGridChange(Grid grid) {
		regenerate();
	}
	
	// build label string for clue based upon clue number and pattern
	private String buildLabelString(Clue clue) {
		return buildLabelString(clue.getNumber(), clue.getCells());
	}
	
	// build label string for clue based upon clue number and pattern from specified cells
	private String buildLabelString(int clueNumber, Cell[] cells) {
		return Integer.toString(clueNumber) + "  " + buildPatternString(cells);
	}
	
	// build pattern string from cells
	private String buildPatternString(Cell[] cells) {
		StringBuilder builder = new StringBuilder(28);
		for (Cell cell : cells)
			builder.append(cell.isEligibleForAutofill() ? "_" : cell.getContents());
		return builder.toString();
	}

	// return whether all the specified cells are filled
	private boolean allCellsFilled(Cell[] cells) {
		for (Cell cell : cells)
			if (cell.isEmpty())
				return false;
		return true;
	}
	
	// enable or disable text field based upon boolean value, clearing field if false
	private void enableOrDisableField(JTextField field, boolean isEnabled) {
		field.setEnabled(isEnabled);
		field.setEditable(isEnabled);
		if (!isEnabled)
			field.setText("");
	}

	// private object to keep track of association between a grid square and its corresponding clues/text fields
	private static class SquareClueAssociation {

		public JLabel acrossLabel;
		public JTextField acrossClueField;
		public Cell[] acrossCells;
		public int acrossClueNumber;
		public JLabel downLabel;
		public JTextField downClueField;
		public Cell[] downCells;
		public int downClueNumber;
	}

}
