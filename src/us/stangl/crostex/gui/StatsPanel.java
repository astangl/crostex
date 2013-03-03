/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridChangeListener;
import us.stangl.crostex.GridWord;
import us.stangl.crostex.util.Message;

/**
 * Panel to display puzzle stats on a side tab.
 * @author Alex Stangl
 */
public final class StatsPanel extends JPanel implements GridChangeListener {

	private static final long serialVersionUID = 1L;

	// reference to grid
	private final Grid grid;

	private JLabel frequencyChartLabel = new JLabel(Message.LABEL_LETTER_FREQUENCY_CHART.toString());
	
	private JTextArea letterFrequencyChart = new JTextArea(1, 40);
	
	public StatsPanel(Grid grid) {
	
		grid.addChangeListener(this);
		this.grid = grid;
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
		add(new JPanel());
		add(new JPanel());
		add(GuiUtils.newJPanel(new FlowLayout(FlowLayout.LEADING), frequencyChartLabel, letterFrequencyChart));
		//add(GuiUtils.flowLayoutPanel(frequencyChartLabel, letterFrequencyChart));
		//add(frequencyChartLabel);
		//add(letterFrequencyChart);
	}
	
	/* (non-Javadoc)
	 * @see us.stangl.crostex.GridChangeListener#handleChange(us.stangl.crostex.Grid)
	 */
	public void handleChange(Grid grid) {
		updateFrequencies(grid);
		letterFrequencyChart.invalidate();
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

}
