/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

/**
 * Value object pair holding an integer (row, column)
 * @author Alex Stangl
 */
public class RowColumnPair implements Comparable<RowColumnPair> {
	public final int row;
	public final int column;
	
	public RowColumnPair(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	@Override
	public int hashCode() {
		return 31 * row + column;
	}
	
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof RowColumnPair))
			return false;
		RowColumnPair that = (RowColumnPair)other;
		return this.row == that.row && this.column == that.column;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Follows a row-major ranking, so row trumps column in comparison.
	 */
	@Override
	public int compareTo(RowColumnPair that) {
		// performing repeated comparisons instead of subtractions to avoid overflow possibility
		if (this.row < that.row)
			return -1;
		if (this.row > that.row)
			return 1;
		if (this.column < that.column)
			return -1;
		if (this.column > that.column)
			return 1;
		return 0;
	}
}
