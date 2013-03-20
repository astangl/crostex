/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Object representing a single cell/box in a crossword puzzle.
 * @author Alex Stangl
 */
public class Cell {
	// cell contents, when string contents
	private String stringContents = "";
	
	// cell number, if any; 0 means no cell number
	private int number;
	
	// whether this cell is filled in black
	private boolean black;
	
	// whether this cell is circled
	private boolean circled;
	
	// if this cell is a rebus, associated rebus data, else null
	private Rebus rebus;
	
	public boolean isBlack() {
		return black;
	}
	
	public void setBlack(boolean black) {
		this.black = black;
	}
	
	public void toggleBlack() {
		black = !black;
	}

	/**
	 * @return whether this is a non-black, empty cell
	 */
	public boolean isEmpty() {
		return !black && stringContents.length() == 0;
	}
	
	public void setEmpty() {
		black = false;
		stringContents = "";
	}

	public String getContents() {
		return stringContents;
	}

	/** for compatability setContents(null) and getContents() == null can be used to set/check BLACK. Better to use setBlack/isBlack though. */
	public void setContents(String contents) {
		stringContents = contents;
	}
	
	/**
	 * Copy state from the specified cell, making this cell an exact copy of other.
	 * @param other cell to copy in to this one
	 */
	public void copyFrom(Cell other) {
		stringContents = other.stringContents;
		number = other.number;
		black = other.black;
		circled = other.circled;
	}

	/** append contents to the builder in the most efficient way possible */
	public void appendContents(StringBuilder builder) {
		if (! black)
			builder.append(stringContents);
	}
	
	/** append contents to the array in the most efficient way possible */
	public void appendContents(char[] array, int fillPointer) {
		if (! black) {
			for (int i = 0; i < stringContents.length(); ++i) {
				array[fillPointer + i] = stringContents.charAt(i);
			}
		}
	}
	
	/** confirm that cell contents agree with the contents specified */
	public boolean confirmContents(char[] array, int startIndex, int len) {
		if (getContentsSize() != len)
			return false;
		if (! black) {
			for (int i = 0; i < stringContents.length(); ++i)
				if (stringContents.charAt(i) != array[startIndex + i])
					return false;
			return true;
		}
		return true;
	}
	
	public int getContentsSize() {
		if (! black)
			return stringContents.length();
		return 0;
	}
	
	/**
	 * @return number associated with cell (0 means no number)
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Set number associated with cell.
	 * @param number number associated with cell (0 means no number)
	 */
	public void setNumber(int number) {
		this.number = number;
	}
	
	/**
	 * @return whether cell is eligible for auto-fill (empty, and not black)
	 */
	public boolean isEligibleForAutofill() {
		return !black && stringContents.length() == 0;
	}
	
	/**
	 * @return the circled
	 */
	public boolean isCircled() {
		return circled;
	}

	/**
	 * @param circled the circled to set
	 */
	public void setCircled(boolean circled) {
		this.circled = circled;
	}
	
	public String toString() {
		return new StringBuilder()
			.append(", stringContents = ")
			.append(stringContents)
			.append(", number = ")
			.append(number)
			.append(", black = ")
			.append(black)
			.append(", circled =")
			.append(circled)
			.toString();
	}
}
