/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Object representing a single clue for a crossword puzzle.
 * @author Alex Stangl
 */
public class Clue {

	// corresponding word in the grid
	private String gridWord;
	
	// number of the word in the grid
	private int wordNumber;
	
	// whether this is an across or down clue
	private AcrossDownDirection acrossDown;
	
	// text of the clue
	private String clueText;

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
	 * @return the wordNumber
	 */
	public int getWordNumber() {
		return wordNumber;
	}

	/**
	 * @param wordNumber the wordNumber to set
	 */
	public void setWordNumber(int wordNumber) {
		this.wordNumber = wordNumber;
	}

	/**
	 * @return the acrossDown
	 */
	public AcrossDownDirection getAcrossDown() {
		return acrossDown;
	}

	/**
	 * @param acrossDown the acrossDown to set
	 */
	public void setAcrossDown(AcrossDownDirection acrossDown) {
		this.acrossDown = acrossDown;
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
	
}
