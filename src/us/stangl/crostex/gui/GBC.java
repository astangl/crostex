/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.GridBagConstraints;

/**
 * Convenience class that extends GridBagConstraints to allow easily creating
 * and manipulating them with chained methods.
 * Idea courtesy of Cay Horstmann
 * @author Alex Stangl
 */
public class GBC extends GridBagConstraints {
	private static final long serialVersionUID = 1L;

	public GBC() {
	}
	
	public GBC(int gridx, int gridy) {
		this.gridx = gridx;
		this.gridy = gridy;
	}
	
	public GBC gridx(int value) {
		this.gridx = value;
		return this;
	}
	
	public GBC gridy(int value) {
		this.gridy = value;
		return this;
	}
	
	public GBC weightx(double value) {
		this.weightx = value;
		return this;
	}
	
	public GBC weighty(double value) {
		this.weighty = value;
		return this;
	}
	
	public GBC gridwidth(int value) {
		this.gridwidth = value;
		return this;
	}
	
	public GBC anchor(int value) {
		this.anchor = value;
		return this;
	}
}
