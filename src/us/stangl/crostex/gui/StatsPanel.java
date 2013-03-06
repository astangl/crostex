/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridChangeListener;
import us.stangl.crostex.GridWord;
import us.stangl.crostex.constraint.GridConstraint;
import us.stangl.crostex.constraint.Min3LetterWordGridConstraint;
import us.stangl.crostex.constraint.OnePolyominoGridConstraint;
import us.stangl.crostex.constraint.SymmetryGridConstraint;
import us.stangl.crostex.util.Message;

/**
 * Panel to display puzzle stats on a side tab.
 * @author Alex Stangl
 */
public final class StatsPanel extends JPanel implements GridChangeListener {

	private static final long serialVersionUID = 1L;
	
	// text field length (for title/author/copyright/...)
	private static final int TEXT_FIELD_LENGTH = 40;
	
	// single polyomino constraint
	private static final GridConstraint SINGLE_POLYOMINO_CONSTRAINT = new OnePolyominoGridConstraint();
	
	// minimum 3-letter word grid constraint
	private static final GridConstraint MIN_3_LETTER_WORD_CONSTRAINT = new Min3LetterWordGridConstraint();

	// symmetry constraint
	private static final GridConstraint SYMMETRY_CONSTRAINT = new SymmetryGridConstraint();

	// reference to associated CrosswordPanel
	private final CrosswordPanel crosswordPanel;
	
	// reference to grid
	private final Grid grid;

	private JLabel frequencyChartLabel = new JLabel(Message.LABEL_LETTER_FREQUENCY_CHART.toString());
	
	// text field for crossword title
	private final JTextField titleField;
	
	// text field for crossword author
	private final JTextField authorField;
	
	// text field for copyright notice
	private final JTextField copyrightField;
	
	// checkbox field for enforcing symmetry
	private final JCheckBox enforceSymmetryField;
	
	private final JCheckBox showNumbersField;
	
	//private final JComboBox cursorSkippingBehaviorField;
	
	private final JCheckBox wraparoundCursorField;
	
	private final JLabel singlePolyominoConstraintMetLabel = newLabel(Message.MESSAGE_SINGLE_POLYOMINO_CONSTRAINT_MET);
	private final JLabel singlePolyominoConstraintViolatedLabel = newLabel(Message.MESSAGE_SINGLE_POLYOMINO_CONSTRAINT_VIOLATED);
	
	private final JLabel min3LetterWordConstraintMetLabel = newLabel(Message.MESSAGE_MIN_3_LETTER_WORD_CONSTRAINT_MET);
	private final JLabel min3LetterWordConstraintViolatedLabel = newLabel(Message.MESSAGE_MIN_3_LETTER_WORD_CONSTRAINT_VIOLATED);
	
	private final JLabel symmetricGridConstraintMetLabel = newLabel(Message.MESSAGE_SYMMETRIC_GRID_CONSTRAINT_MET);
	private final JLabel symmetricGridConstraintViolatedLabel = newLabel(Message.MESSAGE_SYMMETRIC_GRID_CONSTRAINT_VIOLATED);
	
	// text area used for displaying letter frequency chart
	private JTextArea letterFrequencyChart = new JTextArea(1, 40);
	
	public StatsPanel(CrosswordPanel crosswordPanel) {
	
		this.crosswordPanel = crosswordPanel;
		grid = this.crosswordPanel.getGrid();
		grid.addChangeListener(this);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		frequencyChartLabel.setLabelFor(letterFrequencyChart);
		frequencyChartLabel.setAlignmentY(TOP_ALIGNMENT);
		
		// set letter frequency chart to wrap, at whitespace break
		updateFrequencies(grid);
		letterFrequencyChart.setLineWrap(true);
		letterFrequencyChart.setWrapStyleWord(true);
		letterFrequencyChart.setEditable(false);
		letterFrequencyChart.setBackground(super.getBackground());
		letterFrequencyChart.setAlignmentY(TOP_ALIGNMENT);
		
		GridBagLayout gbl = new GridBagLayout();
		JPanel topPanel = new JPanel();
		topPanel.setLayout(gbl);
		titleField = new JTextField(this.grid.getTitle(), TEXT_FIELD_LENGTH);
		titleField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				StatsPanel.this.grid.setTitle(titleField.getText());
			}
		});
		
		// when title field loses focus, treat it as a "commit", so hitting Enter is not necessary
		GuiUtils.addListenerToCommitOnFocusLost(titleField);

		authorField = new JTextField(this.grid.getAuthor(), TEXT_FIELD_LENGTH);
		authorField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				StatsPanel.this.grid.setAuthor(authorField.getText());
			}
		});
		GuiUtils.addListenerToCommitOnFocusLost(authorField);
		
		copyrightField = new JTextField(this.grid.getCopyright(), TEXT_FIELD_LENGTH);
		copyrightField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				StatsPanel.this.grid.setCopyright(copyrightField.getText());
			}
		});
		GuiUtils.addListenerToCommitOnFocusLost(copyrightField);
		
		enforceSymmetryField = new JCheckBox(Message.LABEL_ENFORCE_SYMMETRY.toString(), grid.isMaintainingSymmetry());
		enforceSymmetryField.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				StatsPanel.this.grid.setMaintainingSymmetry(enforceSymmetryField.isSelected());
			}
		});
		
		showNumbersField = new JCheckBox(Message.LABEL_SHOW_NUMBERS.toString(), grid.isDisplayingWordNumbers());
		showNumbersField.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				StatsPanel.this.grid.setDisplayingWordNumbers(showNumbersField.isSelected());
				StatsPanel.this.crosswordPanel.repaint(0);
			}
		});

		wraparoundCursorField = new JCheckBox(Message.LABEL_WRAPAROUND_CURSOR.toString(), grid.isWrappingCursor());
		wraparoundCursorField.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				StatsPanel.this.grid.setWrappingCursor(wraparoundCursorField.isSelected());
			}
		});
		singlePolyominoConstraintViolatedLabel.setForeground(Color.RED);
		min3LetterWordConstraintViolatedLabel.setForeground(Color.RED);
		symmetricGridConstraintViolatedLabel.setForeground(Color.RED);
		topPanel.add(newLabel(Message.LABEL_TITLE), GuiUtils.northWestAnchorConstraints(0, 0));
		topPanel.add(Box.createHorizontalStrut(10));
		topPanel.add(titleField, GuiUtils.gridwidthRemainderNorthWestConstraints(2, 0));
		topPanel.add(newLabel(Message.LABEL_AUTHOR), GuiUtils.northWestAnchorConstraints(0, 1));
		topPanel.add(authorField, GuiUtils.gridwidthRemainderNorthWestConstraints(2, 1));
		topPanel.add(newLabel(Message.LABEL_COPYRIGHT), GuiUtils.newConstraints(0, 2));
		topPanel.add(copyrightField, GuiUtils.gridwidthRemainderNorthWestConstraints(2, 2));
		topPanel.add(GuiUtils.newJPanel(new FlowLayout(FlowLayout.LEFT, 0, 0), singlePolyominoConstraintMetLabel, singlePolyominoConstraintViolatedLabel), GuiUtils.gridwidthRemainderNorthWestConstraints(0, 3));
		topPanel.add(GuiUtils.newJPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), min3LetterWordConstraintMetLabel, min3LetterWordConstraintViolatedLabel), GuiUtils.gridwidthRemainderNorthWestConstraints(0, 4));
		topPanel.add(GuiUtils.newJPanel(new FlowLayout(FlowLayout.LEADING, 0, 0), symmetricGridConstraintMetLabel, symmetricGridConstraintViolatedLabel), GuiUtils.gridwidthRemainderNorthWestConstraints(0, 5));
		topPanel.add(enforceSymmetryField, GuiUtils.gridwidthRemainderNorthWestConstraints(2, 6));
		topPanel.add(showNumbersField, GuiUtils.gridwidthRemainderNorthWestConstraints(2, 7));
		topPanel.add(wraparoundCursorField, GuiUtils.gridwidthRemainderNorthWestConstraints(2, 8));
		
		add(topPanel);
		add(GuiUtils.newJPanel(new FlowLayout(FlowLayout.LEADING), frequencyChartLabel, letterFrequencyChart));
		setFieldsForGrid();
	}
	
	/* (non-Javadoc)
	 * @see us.stangl.crostex.GridChangeListener#handleChange(us.stangl.crostex.Grid)
	 */
	public void handleChange(Grid grid) {
		updateFrequencies(grid);
		letterFrequencyChart.invalidate();
		
		setFieldsForGrid();
	}
	
	private void setFieldsForGrid() {
		boolean symmetric = SYMMETRY_CONSTRAINT.satisfiedBy(grid);
		boolean singlePolyomino = SINGLE_POLYOMINO_CONSTRAINT.satisfiedBy(grid);
		boolean threeLetterWordConstraintMet = MIN_3_LETTER_WORD_CONSTRAINT.satisfiedBy(grid);

		singlePolyominoConstraintMetLabel.setVisible(singlePolyomino);
		singlePolyominoConstraintViolatedLabel.setVisible(!singlePolyomino);
		
		min3LetterWordConstraintMetLabel.setVisible(threeLetterWordConstraintMet);
		min3LetterWordConstraintViolatedLabel.setVisible(!threeLetterWordConstraintMet);
		
		symmetricGridConstraintMetLabel.setVisible(symmetric);
		symmetricGridConstraintViolatedLabel.setVisible(!symmetric);
	}
	
	private void updateFrequencies(Grid grid) {
		int[] freqs = new int[26];
		for (GridWord word : grid.getAcrossWords())
			for (char c : word.getPattern())
				if (c >= 'A' && c <= 'Z')
					++freqs[c - 'A'];
		StringBuilder labelText = new StringBuilder();
		for (char c = 'A'; c <= 'Z'; ++c)
			labelText.append(c).append(": ").append(Integer.toString(freqs[c - 'A'])).append("  ");

		letterFrequencyChart.setText(labelText.toString());
	}
	
	// return new label, with specified message
	private JLabel newLabel(Message message) {
		return new JLabel(message.toString());
	}
}
