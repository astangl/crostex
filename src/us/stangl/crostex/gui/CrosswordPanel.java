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

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import us.stangl.crostex.CrosswordPuzzle;
import us.stangl.crostex.Grid;
import us.stangl.crostex.ServiceException;
import us.stangl.crostex.util.Message;

public class CrosswordPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(CrosswordPanel.class.getName());

	// parent (enclosing) MainFrame
	private MainFrame parentFrame;
	
	// preferred size dimensions
	private static final Dimension PREFERRED_SIZE = new Dimension(300, 300);
	
	// associated crossword puzzle
	private final CrosswordPuzzle crossword;
	
	// popup menu for cells
	private JPopupMenu cellPopupMenu = new JPopupMenu();
	
	
	public CrosswordPanel(MainFrame parentFrame, Grid grid) {
		this.parentFrame = parentFrame;
		this.crossword = new CrosswordPuzzle(grid);
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		MouseEventListener mouseListener = new MouseEventListener();
		this.addMouseListener(mouseListener);
		
		this.setFocusable(true);
		this.requestFocusInWindow();
		
		KeyEventListener keyListener = new KeyEventListener();
		this.addKeyListener(keyListener);
		
		this.crossword.renumberCells();
		
		JMenuItem toggleCellBlackWhiteItem = new JMenuItem(Message.CELL_POPUP_MENU_OPTION_TOGGLE_CELL_BLACK.toString());
		cellPopupMenu.add(toggleCellBlackWhiteItem);
		toggleCellBlackWhiteItem.setActionCommand("Toggle cell between black/white");
		toggleCellBlackWhiteItem.addActionListener(new ToggleCellActionListener());
	}
	
	/**
	 * @return preferred size
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}
	
	/**
	 * 
	 */
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		super.paintComponent(g);
		
		// Draw grid
		crossword.render(g2);
	}
	
	public void save(String dataDirectory, String filename) throws ServiceException {
		crossword.save(dataDirectory, filename);
	}
	
	private class MouseEventListener extends MouseAdapter {
		// Have to override pressed, clicked, and released because popup trigger could
		// be associated with any of the 3, depending upon the platform
		@Override
		public void mouseClicked(MouseEvent evt) {
			boolean rfiwReturn = requestFocusInWindow();
			LOG.finest("requestFocusInWindow returned " + rfiwReturn);
			crossword.mouseClicked(evt);
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
				cellPopupMenu.show(CrosswordPanel.this, evt.getX(), evt.getY());
			}
		}
	}
	
	private class KeyEventListener extends KeyAdapter {
		@Override
		public void keyTyped(KeyEvent evt) {
			crossword.keyTyped(evt);
			parentFrame.resetEditMenuState();
			CrosswordPanel.this.repaint(0);
		}
	}
	
	private class ToggleCellActionListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			crossword.toggleCurrentCell();
			crossword.renumberCells();
			parentFrame.resetEditMenuState();
			CrosswordPanel.this.repaint(0);
		}
	}

	public CrosswordPuzzle getCrossword() {
		return crossword;
	}
	
//	private Document getStateAsDocument() {
//		//TODO need to implement this!
//		return new Document();
//	}
	/*
XML syntax:
<crosswordpuzzle>

	 */
}
