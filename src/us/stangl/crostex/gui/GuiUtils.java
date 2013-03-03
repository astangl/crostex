/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * Miscellaneous GUI-related static utility methods.
 * @author Alex Stangl
 */
public class GuiUtils {

	/**
	 * Return new JPanel with the specified components laid out in a BoxLayout along the X-axis
	 * @param components components to place in JPanel
	 * @return new JPanel
	 */
	public static JPanel xBoxLayoutPanel(Component... components) {
		return newJPanel(BoxLayout.X_AXIS, components);
	}
	
	/**
	 * Return new JPanel with the specified components laid out in a BoxLayout along the Y-axis
	 * @param components components to place in JPanel
	 * @return new JPanel
	 */
	public static JPanel yBoxLayoutPanel(Component... components) {
		return newJPanel(BoxLayout.Y_AXIS, components);
	}
	
	public static JPanel flowLayoutPanel(Component... components) {
		return newJPanel(new FlowLayout(), components);
	}
	
	// return new JPanel with BoxLayout along the specified axis, containing specified components
	private static JPanel newJPanel(int layoutAxis, Component... components) {
		JPanel retval = new JPanel();
		retval.setLayout(new BoxLayout(retval, layoutAxis));
		for (Component component : components)
			retval.add(component);
		return retval;
	}
	
	/**
	 * Return new JPanel with specified LayoutManager, containing specified components
	 * @param layoutManager layout manager to use
	 * @param components components to place into panel
	 * @return new JPanel with specified LayoutManager, containing specified components
	 */
	public static JPanel newJPanel(LayoutManager layoutManager, Component... components) {
		JPanel retval = new JPanel();
		retval.setLayout(layoutManager);
		for (Component component : components)
			retval.add(component);
		return retval;
	}
}
