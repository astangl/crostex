/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridWord;

/**
 * Panel to display puzzle stats on a side tab.
 * @author Alex Stangl
 */
public class StatsPanel extends JPanel {
	// reference back to MainFrame
	private final MainFrame mainFrame;
	
	// reference to grid
	private final Grid grid;

	private JLabel label1 = new JLabel("Label 1");
	
	private JLabel label2 = new JLabel("Label 2");
	
	public StatsPanel(MainFrame mainFrame, Grid grid) {
	
		this.mainFrame = mainFrame;
		this.grid = grid;
		add(label1);
		add(label2);
	}
	
	private void updateFrequencies() {
		int[] freqs = new int[26];
		for (GridWord word : grid.getAcrossWords())
			for (char c : word.getPattern())
				if (c >= 'A' && c <= 'Z')
					++freqs[c - 'A'];
		StringBuilder labelText = new StringBuilder();
		for (char c = 'A'; c <= 'Z'; ++c) {
			labelText.append(c).append(": ").append(Integer.toString(freqs[c - 'A']));
		}
		label2.setText(labelText.toString());
	}

}
