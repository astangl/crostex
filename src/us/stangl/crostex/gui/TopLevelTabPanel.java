/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import us.stangl.crostex.Grid;
import us.stangl.crostex.util.Message;

/**
 * Panel containing all content for a top-level tab.
 * @author Alex Stangl
 */
public class TopLevelTabPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	// panel holding crossword grid
	private final CrosswordPanel crosswordPanel;

	// tabbed pane holding other panes related to the grid
	private final JTabbedPane tabbedPane = new JTabbedPane();
	
	public TopLevelTabPanel(MainFrame mainFrame, Grid grid) {

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		crosswordPanel = new CrosswordPanel(mainFrame, grid);
		add(crosswordPanel);
		add(tabbedPane);
		tabbedPane.addTab(Message.STATS_TAB_TITLE.toString(), new StatsPanel(mainFrame, grid));
		tabbedPane.addTab(Message.CLUES_TAB_TITLE.toString(), new SideTabPanel(mainFrame, grid));
		tabbedPane.setSelectedIndex(0);
	}

	/**
	 * @return the crosswordPanel
	 */
	public CrosswordPanel getCrosswordPanel() {
		return crosswordPanel;
	}

	/**
	 * @return the tabbedPane
	 */
	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}
}
