/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import org.junit.Test;

import us.stangl.crostex.Grid;
import us.stangl.crostex.ServiceException;

/**
 * JUnit tests for CrosswordPanel.
 */
public class CrosswordPanelTest {
	@Test
	public void testCrosswordSave() throws ServiceException
	{
		Grid grid = new Grid(15, 15, "dummy", "dummy");
		CrosswordPanel panel = new CrosswordPanel(grid);
		String tempDirectory = "/tmp";
		String tempFilename = "CrosswordPanelTest.testCrosswordSave.deleteme";
		panel.save(tempDirectory, tempFilename);
	}
}
