/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import us.stangl.crostex.GridWord.Direction;

/**
 * Crossword grid.
 * @author alex
 */
public class Grid 
{
	/** logger */
	private static final Logger LOG = Logger.getLogger(Grid.class.getName());

	/** cell width */
	private int CELL_WIDTH = 24;
	
	/** cell height */
	private int CELL_HEIGHT = 24;
	
	/** grid width, in number of cells */
	private final int width_;
	
	/** height, in number of cells */
	private final int height_;
	
	/** name of grid */
	private String name_;
	
	/** description for grid */
	private String description_;
	
	/** 2-dimensional array of Cells representing this grid */
	private final Cell[][] cells_;

	/**
	 * Constructor for Grid
	 * @param width width of grid, in number of cells
	 * @param height height of grid, in number of cells
	 * @param name short name for grid
	 * @param description longer description for grid
	 */
	public Grid(int width, int height, String name, String description)
	{
		width_ = width;
		height_ = height;
		name_ = name;
		description_ = description;
		cells_ = new Cell[height_][width_];
		for (int row = 0; row < height; ++row) {
			for (int col = 0; col < width; ++col) {
				cells_[row][col] = new Cell();
			}
		}
	}
	
	/**
	 * Copy constructor, copies another Grid
	 * @param gridToCopy grid to make copy of
	 */
	public Grid(Grid gridToCopy) {
		this(gridToCopy.getWidth(), gridToCopy.getHeight(), gridToCopy.getName(), gridToCopy.getDescription());
		for (int row = 0; row < height_; ++row) {
			for (int col = 0; col < width_; ++col) {
				getCell(row, col).setContents(gridToCopy.getCell(row, col).getContents());
			}
		}
	}

	/**
	 * Return whether this grid is structurally equivalent to the specified grid, i.e., same dimensions, same pattern of blacks.
	 * @param other
	 * @return whether this grid is structurally equivalent to the specified grid, i.e., same dimensions, same pattern of blacks
	 */
	public boolean isStructureEqualTo(Grid other) {
		if (getWidth() != other.getWidth() || getHeight() != other.getHeight())
			return false;
		
		for (int row = 0; row < height_; ++row)
			for (int col = 0; col < width_; ++col)
				if (getCell(row, col).isBlack() != other.getCell(row, col).isBlack())
					return false;
		return true;
	}

	/**
	 * Render grid to graphics context.
	 * @param g graphics context
	 */
	public void render(Graphics2D g) {
		render(g, getCellWidth(), getCellHeight(), false);
	}
	
	/**
	 * Render grid to graphics context.
	 * @param g graphics context
	 */
	public void renderThumbnail(Graphics2D g, int containerWidth, int containerHeight) {
		render(g, containerWidth / getWidth(), containerHeight / getHeight(), true);
	}
	

	/**
	 * Render grid to graphics context.
	 * @param g graphics context
	 */
	public void render(Graphics2D g, int cellWidth, int cellHeight, boolean thumbnail) {
		Font font = g.getFont();
		FontMetrics fontMetrics = g.getFontMetrics();
		int ascent = fontMetrics.getAscent();
		int cellHeightResidual = cellHeight - ascent;
		int cellWidthResidual = cellWidth - fontMetrics.charWidth('X');
		
		Font smallFont = font.deriveFont(8.0f);
		LOG.finest("font size = " + font.getSize() + ", cellHeightResidual = " + cellHeightResidual
				+ ", cellWidthResidual = " + cellWidthResidual);				
		for (int row = 0; row < height_; ++row) {
			for (int col = 0; col < width_; ++col) {
				int charCode = row * height_ + col;
				Cell cell = getCell(row, col);
				if (cell.isBlack()) {
					g.fillRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
				} else {
					if (! thumbnail) {
						char myChar = (char)((charCode % 26) + 'A');
//						g.drawString("" + myChar, col * cellWidth + cellWidthResidual / 2, row * cellHeight + ascent + cellHeightResidual / 2);
						g.drawString(cell.getContents(), col * cellWidth + cellWidthResidual / 2, row * cellHeight + ascent + cellHeightResidual / 2);
						if (cell.getNumber() > 0) {
							g.setFont(smallFont);
	//						g.drawString("" + (row * height_ + col), col * cellWidth, row * cellHeight + 8);
							g.drawString("" + cell.getNumber(), col * cellWidth, row * cellHeight + 8);
							g.setFont(font);
						}
					}
				}
				g.drawRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
				
			}
		}
//		g.fillRect(0, 0, 30, 30);
	}

	public void printToStream(PrintStream stream) {
		StringBuilder temp = new StringBuilder();
		for (int row = 0; row < height_; ++row) {
			for (int col = 0; col < width_; ++col) {
				int charCode = row * height_ + col;
				Cell cell = getCell(row, col);
				if (cell.isBlack()) {
					temp.append('#');
				} else if (cell.isEmpty()) {
					temp.append(' ');
				} else {
					temp.append(cell.getContents());
				}
			}
			stream.println(temp.toString());
			temp.setLength(0);
		}
	}
	
	public void renumberCells() {
		int currNumber = 1;
		
		for (int row = 0; row < height_; ++row)
			for (int col = 0; col < width_; ++col)
				cells_[row][col].setNumber(isStartOfAcrossWord(row, col) || isStartOfDownWord(row, col) ? currNumber++ : 0);
	}

	/**
	 * Return whether the specified (row, col) starts an across word.
	 * It starts an across word if it is not black AND (leftmost on the line, or has a black to its left) AND non-black to the right.
	 * @param row 0-based row coordinate
	 * @param col 0-based column coordinate
	 * @return whether the specified (row, col) starts an across word
	 */
	public boolean isStartOfAcrossWord(int row, int col) {
		return ! cells_[row][col].isBlack() && ((col == 0 || cells_[row][col - 1].isBlack()) && (col < width_ - 1 && ! cells_[row][col + 1].isBlack()));
	}

	/**
	 * Return whether the specified (row, col) starts a down word.
	 * It starts an down word if it is not black AND (it is topmost in its column, or has a black above it) AND and non-black below it.
	 * @param row 0-based row coordinate
	 * @param col 0-based column coordinate
	 * @return whether the specified (row, col) starts a down word
	 */
	public boolean isStartOfDownWord(int row, int col) {
		return ! cells_[row][col].isBlack() && ((row == 0 || cells_[row - 1][col].isBlack()) && (row < height_ - 1 && ! cells_[row + 1][col].isBlack()));
	}

	/**
	 * @param row row
	 * @param col column
	 * @return cell based upon 0-based row and col
	 */
	public Cell getCell(int row, int col) {
		return cells_[row][col];
	}
	
	private Cell getCellForXY(int x, int y) {
		if (x < 0 || x > getCellWidth() * width_ || y < 0 || y > getCellHeight() * height_)
			return null;
		int row = y / getCellHeight();
		int col = x / getCellWidth();
		return getCell(row, col);
	}
	
	private int getCellWidth()	{
		return CELL_WIDTH;
	}
	
	private int getCellHeight()	{
		return CELL_HEIGHT;
	}
	
	public void mouseClicked(MouseEvent evt) {
		LOG.finest("mouse clicked at " + evt.getX() + ", " + evt.getY());
		Cell cell = getCellForXY(evt.getX(), evt.getY());
		if (cell != null) {
			cell.setContents("A");
			renumberCells();
		}
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height_;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width_;
	}
	
	public int getNumberBlackCells() {
		int retval = 0;
		for (int row = 0; row < height_; ++row)
			for (int col = 0; col < width_; ++col)
				if (cells_[row][col].isBlack())
					++retval;
		return retval;
	}
	
	public int getNumberOfWords() {
		int retval = 0;
		for (int row = 0; row < height_; ++row)
			for (int col = 0; col < width_; ++col) {
				if (isStartOfAcrossWord(row, col))
					retval++;
				if (isStartOfDownWord(row, col))
					retval++;
			}
		return retval;
	}
	
	public List<GridWord> getAcrossWords() {
		int counter = 0;
		List<GridWord> retval = new ArrayList<GridWord>();
		for (int row = 0; row < height_; ++row)
			for (int col = 0; col < width_; ++col) {
				if (isStartOfAcrossWord(row, col)) {
					counter++;
					int colEnd = col;
					while (colEnd < width_ && ! getCell(row, colEnd).isBlack())
						colEnd++;
					Cell[] cells = new Cell[colEnd - col];
					for (int i = 0; i < cells.length; ++i)
						cells[i] = getCell(row, col + i);
					retval.add(new GridWord(cells, Direction.ACROSS, counter));
				}
				else if (isStartOfDownWord(row, col))
					counter++;
			}
		return retval;
	}
	
	public List<GridWord> getDownWords() {
		int counter = 0;
		List<GridWord> retval = new ArrayList<GridWord>();
		for (int row = 0; row < height_; ++row)
			for (int col = 0; col < width_; ++col) {
				if (isStartOfDownWord(row, col)) {
					counter++;
					int rowEnd = row;
					while (rowEnd < height_ && ! getCell(rowEnd, col).isBlack())
						rowEnd++;
					Cell[] cells = new Cell[rowEnd - row];
					for (int i =0; i < cells.length; ++i)
						cells[i] = getCell(row + i, col);
					retval.add(new GridWord(cells, Direction.DOWN, counter));
				}
				else if (isStartOfAcrossWord(row, col))
					counter++;
			}
		return retval;
	}
	
	public boolean autoFill(Dictionary<char[], Word> dict) {
		return new AutoFiller7().autoFill(this, dict);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description_;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		description_ = description;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name_;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		name_ = name;
	}
}
