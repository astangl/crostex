/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
	
	/**
	 * Return a GridBagConstraints with anchor in NORTHWEST corner and specified gridx, gridy
	 * @param gridx gridx to place into GridBagConstraints
	 * @param gridy gridy to place into GridBagConstraints
	 * @return GridBagConstraints with anchor in NORTHWEST corner and specified gridx, gridy
	 */
	public static GridBagConstraints northWestAnchorConstraints(int gridx, int gridy) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		return gbc;
	}
	
	/**
	 * Return a GridBagConstraints with gridwidth REMAINDER, weightx = 1.0, and specified gridx, gridy
	 * @param gridx gridx to place into GridBagConstraints
	 * @param gridy gridy to place into GridBagConstraints
	 * @return GridBagConstraints with gridwidth REMAINDER, weightx = 1.0, and specified gridx, gridy
	 */
	public static GridBagConstraints gridwidthRemainderConstraints(int gridx, int gridy) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		return gbc;
	}
	
	/**
	 * Return a GridBagConstraints with gridwidth REMAINDER, weightx = 1.0, anchor NORTHWEST, and specified gridx, gridy
	 * @param gridx gridx to place into GridBagConstraints
	 * @param gridy gridy to place into GridBagConstraints
	 * @return GridBagConstraints with gridwidth REMAINDER, weightx = 1.0, anchor NORTHWEST, and specified gridx, gridy
	 */
	public static GridBagConstraints gridwidthRemainderNorthWestConstraints(int gridx, int gridy) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.weightx = 1.0;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		return gbc;
	}
	
	/**
	 * Return a GridBagConstraints with specified gridx, gridy
	 * @param gridx gridx to place into GridBagConstraints
	 * @param gridy gridy to place into GridBagConstraints
	 * @return GridBagConstraints with specified gridx, gridy
	 */
	public static GridBagConstraints newConstraints(int gridx, int gridy) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		return gbc;
	}
	
	/**
	 * Add Focus listener to text field to commit field whenever it loses focus.
	 * @param field field to commit when losing focus
	 */
	public static void addListenerToCommitOnFocusLost(final JTextField field) {
		field.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent evt) {
				field.postActionEvent();
			}
		});
	}
}
