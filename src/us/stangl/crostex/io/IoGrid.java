/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import java.util.List;

/**
 * Abstract interface for a grid to use for I/O.
 * Using this interface to gain looser coupling and
 * potentially be able to re-use serialization logic.
 * @author Alex Stangl
 */
public interface IoGrid {

	/**
	 * @return title of grid
	 */
	String getTitle();
	
	/**
	 * Set title of grid
	 * @param title new title for grid
	 */
	void setTitle(String title);
	
	/**
	 * @return author of grid
	 */
	String getAuthor();
	
	/**
	 * Set author of grid
	 * @param author new author for grid
	 */
	void setAuthor(String author);
	
	/**
	 * @return copyright for grid
	 */
	String getCopyright();
	
	/**
	 * Set new copyright for grid.
	 * @param copyright new copyright notice
	 */
	void setCopyright(String copyright);
	
	/**
	 * @return notes for grid
	 */
	String getNotes();

	/**
	 * Set notes for grid.
	 * @param notes new notes for grid
	 */
	void setNotes(String notes);
	
	/**
	 * @return width of grid
	 */
	int getWidth();
	
	/**
	 * @return height of grid
	 */
	int getHeight();
	
	/**
	 * Return whether the specified cell is black.
	 * @param row row of the cell
	 * @param column column of the cell
	 * @return whether the specified cell is black
	 */
	boolean isBlackCell(int row, int column);

	/**
	 * Set whether the specified cell is circled or not.
	 * @param row row of the cell
	 * @param column column of the cell
	 * @param isCircled whether the cell is circled (or not)
	 */
	void setCircledCell(int row, int column, boolean isCircled);
	
	/**
	 * Return whether the specified cell is circled.
	 * @param row row of the cell
	 * @param column column of the cell
	 * @return whether the specified cell is circled
	 */
	boolean isCircledCell(int row, int column);

	/**
	 * Set whether the specified cell is black or not.
	 * @param row row of the cell
	 * @param column column of the cell
	 * @param isBlack whether the cell is black (or not)
	 */
	void setBlackCell(int row, int column, boolean isBlack);
	
	/**
	 * Return contents for specified cell, as a String.
	 * Behavior undefined if cell is black. (In other words, calling this
	 * for a black cell is a programming error -- call isBlackCell first.)
	 * @param row row of the cell
	 * @param column column of the cell
	 * @return contents for specified cell, as a String
	 */
	String getCellContents(int row, int column);

	/**
	 * Set contents for specified cell.
	 * @param row row of the cell
	 * @param column column of the cell
	 * @param contents contents for specified cell
	 */
	void setCellContents(int row, int column, String contents);
	
	/**
	 * Return number associated with specified cell, or 0 if cell has no number.
	 * @param row row of the cell
	 * @param column column of the cell
	 */
	int getCellNumber(int row, int column);
	
	/**
	 * @return across clues for puzzle
	 */
	List<? extends IoClue> getAcrossClues();
	
	/**
	 * Set minimal (#/direction/text) across clues.
	 * @param acrossClues
	 */
	void setAcrossClues(List<? extends IoClue> acrossClues);

	/**
	 * @return down clues for puzzle
	 */
	List<? extends IoClue> getDownClues();
	
	/**
	 * Set minimal (#/direction/text) down clues.
	 * @param downClues
	 */
	void setDownClues(List<? extends IoClue> downClues);
	
	/**
	 * @return a new clue
	 */
	IoClue newClue();
}
