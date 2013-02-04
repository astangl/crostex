/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

/**
 * Object representing a single cell/box in a crossword puzzle.
 */
public class Cell {
	enum CellType { EMPTY, BLACK, CHAR, STRING };
	
	/** cell type */
	private CellType cellType_ = CellType.EMPTY;

	/** cell contents, when single char */
	private char charContent_;
	
	/** cell contents, when string contents */
	private String stringContents_ = "";
	
	/** cell number, if any; 0 means no cell number */
	private int number_;
	
	public boolean isBlack() {
		return stringContents_ == null;
	}
	
	public void setBlack() {
		stringContents_ = null;
		cellType_ = CellType.BLACK;
	}

	public boolean isEmpty() {
		return cellType_ == CellType.EMPTY;
	}
	
	public void setEmpty() {
		cellType_ = CellType.EMPTY;
	}
	
	public String getContents() {
		if (cellType_ == CellType.STRING)
			return stringContents_;
		if (cellType_ == CellType.CHAR)
			return "" + charContent_;
		if (cellType_ == CellType.EMPTY)
			return "";
		return null;
	}

	/** for compatability setContents(null) and getContents() == null can be used to set/check BLACK. Better to use setBlack/isBlack though. */
	public void setContents(String contents) {
		stringContents_ = contents;
		cellType_ = contents == null ? CellType.BLACK : 
			(contents.length() == 0 ? CellType.EMPTY : CellType.STRING);
		if (cellType_ == CellType.STRING)
			stringContents_ = contents;
	}

	/** append contents to the builder in the most efficient way possible */
	public void appendContents(StringBuilder builder) {
		if (cellType_ == CellType.STRING)
			builder.append(stringContents_);
		else if (cellType_ == CellType.CHAR)
			builder.append(charContent_);
	}
	
	/** append contents to the array in the most efficient way possible */
	public void appendContents(char[] array, int fillPointer) {
		if (cellType_ == CellType.CHAR)
			array[fillPointer] = charContent_;
		else if (cellType_ == CellType.STRING) {
			for (int i = 0; i < stringContents_.length(); ++i) {
				array[fillPointer + i] = stringContents_.charAt(i);
			}
		}
	}
	
	/** confirm that cell contents agree with the contents specified */
	public boolean confirmContents(char[] array, int startIndex, int len) {
		if (getContentsSize() != len)
			return false;
		if (cellType_ == CellType.CHAR)
			return array[startIndex] == charContent_;
		else if (cellType_ == CellType.STRING) {
			for (int i = 0; i < stringContents_.length(); ++i)
				if (stringContents_.charAt(i) != array[startIndex + i])
					return false;
			return true;
		}
		return true;
	}
	
	public int getContentsSize() {
		if (cellType_ == CellType.CHAR)
			return 1;
		if (cellType_ == CellType.STRING)
			return stringContents_.length();
		return 0;
	}
	
	public void setContents(char c) {
		charContent_ = c;
		cellType_ = CellType.CHAR;
	}
	
	public int getNumber() {
		return number_;
	}
	
	public void setNumber(int number) {
		number_ = number;
	}
	
	public boolean isEligibleForAutofill() {
		return cellType_ == CellType.EMPTY;
//		return multicharContents_ != null && multicharContents_.length() == 0;
	}
	
	
	public String toString() {
		StringBuilder retval = new StringBuilder();
		retval.append("{cellType_ = ")
			.append(cellType_)
			.append(", charContent_ = ")
			.append(charContent_ != '\0' ? charContent_ : "")
			.append(", stringContents_ = ")
			.append(stringContents_)
			.append(", number_ = ")
			.append(number_);
		return retval.toString();
	}
}
