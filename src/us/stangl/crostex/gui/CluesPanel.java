/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import us.stangl.crostex.Grid;
import us.stangl.crostex.GridChangeListener;

/**
 * Panel to display clues on a side tab.
 * @author Alex Stangl
 */
public class CluesPanel extends JPanel implements GridChangeListener {
	private static final long serialVersionUID = 1L;
	
	// associated grid
	private final Grid grid;
	

	public CluesPanel(Grid grid) {
		this.grid = grid;
		
		GridBagLayout gbl = new GridBagLayout();
		JPanel topPanel = new JPanel();
		topPanel.setLayout(gbl);
	}
	
	/* (non-Javadoc)
	 * @see us.stangl.crostex.GridChangeListener#handleChange(us.stangl.crostex.Grid)
	 */
	@Override
	public void handleChange(Grid grid) {
		// TODO Auto-generated method stub
		
	}

}
