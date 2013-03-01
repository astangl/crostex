/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import javax.swing.JPanel;

import us.stangl.crostex.Grid;

/**
 * One of potentially multiple tabbed panels containing side content.
 * @author Alex Stangl
 */
public class SideTabPanel extends JPanel {
	// reference back to MainFrame
	private final MainFrame mainFrame;
	
	// reference to grid
	private final Grid grid;

	public SideTabPanel(MainFrame mainFrame, Grid grid) {
	
		this.mainFrame = mainFrame;
		this.grid = grid;
	}
}
