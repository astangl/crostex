/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import us.stangl.crostex.Grid;
import us.stangl.crostex.util.Message;

/**
 * Panel containing all content for a top-level tab.
 * @author Alex Stangl
 */
public class TopLevelTabPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	// minimum zoom (grid cell width/height)
	private static final int ZOOM_MINIMUM = 10;
	
	// maximum zoom (grid cell width/height)
	private static final int ZOOM_MAXIMUM = 100;
	
	// default zoom factor (grid cell width/height)
	private static final int DEFAULT_ZOOM_VALUE = 24;
	
	// panel holding crossword grid
	private final CrosswordPanel crosswordPanel;

	// tabbed pane holding other panes related to the grid
	private final JTabbedPane tabbedPane = new JTabbedPane();

	private final JSlider zoomSlider = new JSlider();
	
	private final JScrollPane scrollPane;
	
	public TopLevelTabPanel(MainFrame mainFrame, Grid grid) {

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		crosswordPanel = new CrosswordPanel(mainFrame, grid);
		scrollPane = new JScrollPane(crosswordPanel);
		JPanel leftPanel = GuiUtils.yBoxLayoutPanel(scrollPane, zoomSlider);
		
		// set range on zoomSlider
		zoomSlider.setMinimum(ZOOM_MINIMUM);
		zoomSlider.setMaximum(ZOOM_MAXIMUM);
		zoomSlider.setValue(DEFAULT_ZOOM_VALUE);
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Grid grid = crosswordPanel.getGrid();
				int value = zoomSlider.getValue();
				grid.setCellHeight(value);
				grid.setCellWidth(value);
				crosswordPanel.setSize(grid.getPixelWidth(), grid.getPixelHeight());
				scrollPane.revalidate();
				crosswordPanel.repaint(0);
			}
		});
		
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, tabbedPane);
		add(sp);
		tabbedPane.addTab(Message.STATS_TAB_TITLE.toString(), new StatsPanel(crosswordPanel));
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
