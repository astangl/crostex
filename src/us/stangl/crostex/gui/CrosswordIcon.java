/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Crossword grid icon.
 * @author Alex Stangl
 */
public class CrosswordIcon implements Icon {

	// bitmap grid image, one short per row, 1 bits are black
	private static final short[] GRID_DATA = new short[] {
			0x0200, 0x0200, 0x0200, 0x0108, 0x0083,
			0x7040, 0x0020, 0x0410, 0x0200, 0x0107,
			0x6080, 0x0840, 0x0020, 0x0020, 0x0020};
	
	private final int pixelsPerCell;
	
	public CrosswordIcon(int pixelsPerCell) {
		this.pixelsPerCell = pixelsPerCell;
	}

	/**
	 * Draw the icon at the specified location.  Icon implementations
	 * may use the Component argument to get properties useful for
	 * painting, e.g. the foreground or background color.
	 */
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(Color.BLACK);
		for (int row = 0; row < 15; ++row) {
			int bitmap = GRID_DATA[row];
			for (int col = 0; col < 15; ++col) {
				if ((bitmap & 1) != 0) {
					g.fillRect(x + col * pixelsPerCell, y + row * pixelsPerCell, pixelsPerCell, pixelsPerCell);
				}
				g.drawRect(x + col * pixelsPerCell, y + row * pixelsPerCell, pixelsPerCell, pixelsPerCell);
				bitmap >>= 1;
			}
		}
	}

	/**
	 * Returns the icon's width.
	 * @return an int specifying the fixed width of the icon.
	 */
	@Override
	public int getIconWidth() {
		return pixelsPerCell * 15 + 1;
	}

	/**
	 * Returns the icon's height.
	 * @return an int specifying the fixed height of the icon.
	 */
	@Override
	public int getIconHeight() {
		return pixelsPerCell * 15 + 1;
	}
}
