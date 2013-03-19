/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import javax.swing.JPanel;

/**
 * Panel for display/editing of rebus information.
 * @author Alex Stangl
 */
public class RebusPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	// reference to associated CrosswordPanel
	private final CrosswordPanel crosswordPanel;
	
	public RebusPanel(CrosswordPanel crosswordPanel) {
		
		this.crosswordPanel = crosswordPanel;

	}
}
