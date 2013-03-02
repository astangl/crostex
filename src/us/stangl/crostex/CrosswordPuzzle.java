/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import us.stangl.crostex.command.CommandBuffer;
import us.stangl.crostex.command.EnterCharacterToCellCommand;
import us.stangl.crostex.command.SetCurrentCellBlackCommand;
import us.stangl.crostex.command.ToggleCurrentCellCommand;

/**
 * Object representing a crossword puzzle under construction.
 * @author Alex Stangl
 */
public class CrosswordPuzzle {
	// associated crossword grid
	private final Grid grid;
	
	private String title;
	
	private String author;
	
	// command buffer to hold mutating user commands, for undo/redo purposes
	private CommandBuffer<CrosswordPuzzle> commandBuffer = new CommandBuffer<CrosswordPuzzle>(this);
	
	// whether rotational symmetry is being maintained
	private boolean maintainingSymmetry = true;
	
	public CrosswordPuzzle(Grid grid) {
		this.grid = new Grid(grid);
	}

	public void renumberCells() {
		grid.renumberCells();
	}

	public void mouseClicked(MouseEvent evt) {
		grid.mouseClicked(evt);
	}

	public void keyTyped(KeyEvent evt) {
		char c = evt.getKeyChar();
		if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
			c = Character.toUpperCase(c);
			Cell currentCell = grid.getCurrentCell();
			if (currentCell != null && !currentCell.isBlack()) {
				commandBuffer.applyCommand(new EnterCharacterToCellCommand(this, c));
			}
		}
	}
	
	public void toggleCurrentCell() {
		commandBuffer.applyCommand(new ToggleCurrentCellCommand(this));
	}
	
	public void setCurrentCellBlack() {
		commandBuffer.applyCommand(new SetCurrentCellBlackCommand(this));
	}
	
	public Grid getGrid() {
		return grid;
	}
	
	public Cell getCell(int row, int column) {
		return grid.getCell(row, column);
	}
	
	public int getHeight() {
		return grid.getHeight();
	}
	
	public int getWidth() {
		return grid.getWidth();
	}
	
	public int getCurrentRow() {
		return grid.getCurrentRow();
	}
	
	public void setCurrentRow(int currentRow) {
		grid.setCurrentRow(currentRow);
	}
	
	public int getCurrentColumn() {
		return grid.getCurrentColumn();
	}
	
	public void setCurrentColumn(int currentColumn) {
		grid.setCurrentColumn(currentColumn);
	}
	
	public NsewDirection getCurrentDirection() {
		return grid.getCurrentDirection();
	}
	
	public void render(Graphics2D g) {
		grid.render(g);
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
		gridElement.setAttribute("rows", "" + grid.getHeight());
		gridElement.setAttribute("columns", "" + grid.getWidth());
		for (int row = 0; row < grid.getHeight(); ++row) {
			for (int col = 0; col < grid.getWidth(); ++col) {
				Cell cell = grid.getCell(row, col);
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

	public boolean isMaintainingSymmetry() {
		return maintainingSymmetry;
	}

	public void setMaintainingSymmetry(boolean maintainingSymmetry) {
		this.maintainingSymmetry = maintainingSymmetry;
	}
	
	public boolean isAbleToUndo() {
		return commandBuffer.haveCommandsToUndo();
	}

	public boolean isAbleToRedo() {
		return commandBuffer.haveCommandsToRedo();
	}
	
	public void undo() {
		commandBuffer.undo();
	}
	
	public void redo() {
		commandBuffer.redo();
	}
	
	private void notifyChangeListeners() {
		
	}
}
