/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import us.stangl.crostex.io.IoClue;
import us.stangl.crostex.util.RowColumnPair;

/**
 * Object representing a single clue for a crossword puzzle.
 * @author Alex Stangl
 */
public class Clue implements IoClue {

	// corresponding word in the grid
	private String gridWord;

	// flag indicating whether word in grid is complete
	private boolean wordComplete;
	
	// whether this is an across or down clue
	private AcrossDownDirection direction;
	
	// text of the clue
	private String clueText;
	
	// number of the word in the grid
	private int number;
	
	// Start of the corresponding word in the grid (should stay fixed even if wordNumber changes)
	private RowColumnPair startOfWord;
	
	// End of the corresponding word in the grid (should stay fixed even if wordNumber changes)
	private RowColumnPair endOfWord;
	
	// cells comprising the corresponding word in the grid
	private Cell[] cells;

	/**
	 * @return the gridWord
	 */
	public String getGridWord() {
		return gridWord;
	}

	/**
	 * @param gridWord the gridWord to set
	 */
	public void setGridWord(String gridWord) {
		this.gridWord = gridWord;
	}

	/**
	 * @return the number associated with this clue
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @param number the new number associated with this clue
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * @return the direction associated with this clue
	 */
	public AcrossDownDirection getDirection() {
		return direction;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(AcrossDownDirection direction) {
		this.direction = direction;
	}

	/**
	 * @return the clueText
	 */
	public String getClueText() {
		return clueText;
	}

	/**
	 * @param clueText the clueText to set
	 */
	public void setClueText(String clueText) {
		this.clueText = clueText;
	}

	/**
	 * @return the wordComplete
	 */
	public boolean isWordComplete() {
		return wordComplete;
	}

	/**
	 * @param wordComplete the wordComplete to set
	 */
	public void setWordComplete(boolean wordComplete) {
		this.wordComplete = wordComplete;
	}

	/**
	 * @return the startOfWord
	 */
	public RowColumnPair getStartOfWord() {
		return startOfWord;
	}

	/**
	 * @param startOfWord the startOfWord to set
	 */
	public void setStartOfWord(RowColumnPair startOfWord) {
		this.startOfWord = startOfWord;
	}

	/**
	 * @return the endOfWord
	 */
	public RowColumnPair getEndOfWord() {
		return endOfWord;
	}

	/**
	 * @param endOfWord the endOfWord to set
	 */
	public void setEndOfWord(RowColumnPair endOfWord) {
		this.endOfWord = endOfWord;
	}

	/**
	 * @return the cells comprising the corresponding word in the grid
	 */
	public Cell[] getCells() {
		return cells;
	}

	/**
	 * @param cells the cells comprising the corresponding word in the grid to set
	 */
	public void setCells(Cell[] cells) {
		this.cells = cells;
	}
	
}
