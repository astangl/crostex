/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.LayoutFocusTraversalPolicy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import us.stangl.crostex.Cell;
import us.stangl.crostex.DOMSerializer;
import us.stangl.crostex.Grid;
import us.stangl.crostex.ServiceException;

public class CrosswordPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** preferred size dimensions */
	private static final Dimension PREFERRED_SIZE = new Dimension(300, 300);
	
	/** associated crossword grid */
	private final Grid grid_;
	
	public CrosswordPanel(Grid grid) {
		grid_ = grid;
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		MouseEventListener mouseListener = new MouseEventListener();
		this.addMouseListener(mouseListener);
		
		this.setFocusable(true);
		this.requestFocusInWindow();
		
		KeyEventListener keyListener = new KeyEventListener();
		this.addKeyListener(keyListener);
		
		/*
        setFocusCycleRoot(true);
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
                public Component getDefaultComponent(Container cont) {
                    return CrosswordPanel.this;
                }
            });
        */
		grid_.renumberCells();
//		this.setRequestFocusEnabled(true);
//		this.grabFocus();
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
		
		// Draw text
//		g.drawString("This is my custom panel", 10, 20);
		
		// Draw grid
		grid_.render(g2);
	}
	
	@Override
	public boolean isFocusable() {
		return true;
	}
	
	private class MouseEventListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent evt) {
			boolean rfiwReturn = requestFocusInWindow();
			System.out.println("requestFocusInWindow returned " + rfiwReturn);
			System.out.println("isFocusOwner() = " + isFocusOwner());
			Component focusComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if (focusComponent == null) {
				System.out.println("focusComponent is null");
			} else {
				System.out.println("focusComponent is " + focusComponent);
			}
//			Component globalComponent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getGlobalFocusOwner();
//			if (globalComponent == null) {
//				System.out.println("globalComponent is null");
//			} else {
//				System.out.println("globalComponent is " + globalComponent);
//			}
			grid_.mouseClicked(evt);
			CrosswordPanel.this.repaint(0);
		}
	}
	
	private class KeyEventListener extends KeyAdapter {
		@Override
		public void keyTyped(KeyEvent evt) {
			System.out.println("In KeyEventListener.keyTyped");
			grid_.keyTyped(evt);
			CrosswordPanel.this.repaint(0);
		}
		@Override
		public void keyPressed(KeyEvent evt) {
			System.out.println("In KeyEventListener.keyPressed");
		}
	}
	
	public void save(String dataDirectory, String filename) throws ServiceException {
		Document doc = DOMSerializer.newDocument("puzzle");
		Element rootElement = doc.getDocumentElement();
		Element crosswordElement = doc.createElement("crossword");
		crosswordElement.setAttribute("language", "en");
		
		Element metadataElement = doc.createElement("metadata");
		/*
		Element titleElement = doc.createElement("title");
		titleElement.appendChild(doc.createTextNode(getTitle()));
		metadataElement.appendChild(titleElement);
		addOptionalSimpleElement(doc, metadataElement, "date", getDate());
		addOptionalSimpleElement(doc, metadataElement, "creator", getCreator());
		addOptionalSimpleElement(doc, metadataElement, "rights", getRights());
		addOptionalSimpleElement(doc, metadataElement, "publisher", getPublisher());
		addOptionalSimpleElement(doc, metadataElement, "identifier", getIdentifier());
		addOptionalSimpleElement(doc, metadataElement, "description", getDescription());
		*/
		crosswordElement.appendChild(metadataElement);
		
		Element americanElement = doc.createElement("american");
		Element gridElement = doc.createElement("grid");
		gridElement.setAttribute("rows", "" + grid_.getHeight());
		gridElement.setAttribute("columns", "" + grid_.getWidth());
		for (int row = 0; row < grid_.getHeight(); ++row) {
			for (int col = 0; col < grid_.getWidth(); ++col) {
				Cell cell = grid_.getCell(row, col);
				Element cellElement;
				if (cell.isBlack()) {
					cellElement = doc.createElement("blank");
				} else {
					cellElement = doc.createElement("letter");
					cellElement.setAttribute("id", (row + 1) + "," + (col + 1));
					cellElement.appendChild(doc.createTextNode(cell.getContents()));
				}
				gridElement.appendChild(cellElement);
			}
		}
		americanElement.appendChild(gridElement);

		Element cluesElement = doc.createElement("clues");
		
		americanElement.appendChild(cluesElement);
		crosswordElement.appendChild(americanElement);
		rootElement.appendChild(crosswordElement);
		
		//TODO assume DOM tree is complete here -- need to finish population of it
		new DOMSerializer().serialize(doc, new File(dataDirectory, filename));
	}
	
	/** add simple text element to parent if childValue not null, not blank */
	private void addOptionalSimpleElement(Document doc, Element parent, String childName, String childValue) {
		if (childValue != null && childValue.trim().length() > 0) {
			Element childElement = doc.createElement(childName);
			childElement.appendChild(doc.createTextNode(childValue));
			parent.appendChild(childElement);
		}
	}
	
	public Grid getGrid() {
		return grid_;
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
