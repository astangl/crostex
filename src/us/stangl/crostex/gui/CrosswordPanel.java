/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import us.stangl.crostex.Grid;
import us.stangl.crostex.ServiceException;
import us.stangl.crostex.util.Message;

/**
 * JPanel that holds the crossword grid.
 * @author Alex Stangl
 */
public class CrosswordPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(CrosswordPanel.class.getName());
	
	// action map entries for cursor left/right/up/down
	private static final String ACTION_MAP_CURSOR_LEFT = "CURSOR_LEFT";
	private static final String ACTION_MAP_CURSOR_RIGHT = "CURSOR_RIGHT";
	private static final String ACTION_MAP_CURSOR_UP = "CURSOR_UP";
	private static final String ACTION_MAP_CURSOR_DOWN = "CURSOR_DOWN";

	// parent (enclosing) MainFrame
	private MainFrame parentFrame;
	
	// associated grid
	private final Grid grid;
	
	// popup menu for cells
	private JPopupMenu cellPopupMenu = new JPopupMenu();
	
	
	public CrosswordPanel(MainFrame parentFrame, Grid grid) {
		this.parentFrame = parentFrame;
		this.grid = grid;
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		MouseEventListener mouseListener = new MouseEventListener();
		this.addMouseListener(mouseListener);
		
		this.setFocusable(true);
		this.requestFocusInWindow();
		
		KeyEventListener keyListener = new KeyEventListener();
		this.addKeyListener(keyListener);
		addKeystrokeAction(KeyEvent.VK_LEFT, ACTION_MAP_CURSOR_LEFT, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel.this.grid.cursorLeft();
				CrosswordPanel.this.repaint(0);
			}
		});
		addKeystrokeAction(KeyEvent.VK_RIGHT, ACTION_MAP_CURSOR_RIGHT, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel.this.grid.cursorRight();
				CrosswordPanel.this.repaint(0);
			}
		});
		addKeystrokeAction(KeyEvent.VK_UP, ACTION_MAP_CURSOR_UP, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel.this.grid.cursorUp();
				CrosswordPanel.this.repaint(0);
			}
		});
		addKeystrokeAction(KeyEvent.VK_DOWN, ACTION_MAP_CURSOR_DOWN, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {
				CrosswordPanel.this.grid.cursorDown();
				CrosswordPanel.this.repaint(0);
			}
		});

		this.grid.renumberCells();
		
		JMenuItem toggleCellBlackWhiteItem = new JMenuItem(Message.CELL_POPUP_MENU_OPTION_TOGGLE_CELL_BLACK.toString());
		cellPopupMenu.add(toggleCellBlackWhiteItem);
		toggleCellBlackWhiteItem.setActionCommand("Toggle cell between black/white");
		toggleCellBlackWhiteItem.addActionListener(new ToggleCellActionListener());
	}
	
	private void addKeystrokeAction(int keyCode, String actionMapKey, Action action) {
		this.getInputMap().put(KeyStroke.getKeyStroke(keyCode, 0), actionMapKey);
		this.getActionMap().put(actionMapKey, action);
	}
	
	/**
	 * @return preferred size
	 */
	public Dimension getPreferredSize() {
		return new Dimension(grid.getPixelWidth(), grid.getPixelHeight());
	}
	
	/**
	 * Paint this crossword panel in the specified graphics context.
	 * @param g graphics context to paint panel in
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Draw grid
		grid.render((Graphics2D)g);
	}
	
	public void save(String dataDirectory, String filename) throws ServiceException {
		grid.save(dataDirectory, filename);
	}
	
	public Grid getGrid() {
		return grid;
	}
	
	private class MouseEventListener extends MouseAdapter {
		// Have to override pressed, clicked, and released because popup trigger could
		// be associated with any of the 3, depending upon the platform
		@Override
		public void mouseClicked(MouseEvent evt) {
			boolean rfiwReturn = requestFocusInWindow();
			LOG.finest("requestFocusInWindow returned " + rfiwReturn);
			grid.mouseClicked(evt);
			parentFrame.resetEditMenuState();
			CrosswordPanel.this.repaint(0);
			maybeShowPopup(evt);
		}
		@Override
		public void mousePressed(MouseEvent evt) {
			maybeShowPopup(evt);
		}
		@Override
		public void mouseReleased(MouseEvent evt) {
			maybeShowPopup(evt);
		}

		// show popup menu, if appropriate mouse trigger was performed
		private void maybeShowPopup(MouseEvent evt) {
			if (evt.isPopupTrigger()) {
				grid.mouseClicked(evt);
				cellPopupMenu.show(CrosswordPanel.this, evt.getX(), evt.getY());
			}
		}
	}
	
	private class KeyEventListener extends KeyAdapter {
		@Override
		public void keyTyped(KeyEvent evt) {
			grid.keyTyped(evt);
			parentFrame.resetEditMenuState();
			CrosswordPanel.this.repaint(0);
		}
	}
	
	private class ToggleCellActionListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			grid.toggleCurrentCell();
			grid.renumberCells();
			parentFrame.resetEditMenuState();
			CrosswordPanel.this.repaint(0);
		}
	}
}
