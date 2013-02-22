/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import us.stangl.crostex.GridWord.Direction;
import us.stangl.crostex.autofill.AutoFiller7;
import us.stangl.crostex.dictionary.Dictionary;

/**
 * Crossword grid.
 * @author alex
 */
public class Grid 
{
	// logger
	private static final Logger LOG = Logger.getLogger(Grid.class.getName());

	private static final Color LIGHT_YELLOW = new Color(238, 238, 192);
	
	private static final Color VERY_LIGHT_GRAY = new Color(238, 238, 238);

	// cell width
	private int cellWidth = 24;
	
	// cell height
	private int cellHeight = 24;
	
	// x-offset of upper-left corner of grid in graphics context
	private int xoffset = 0;
	
	// y-offset of upper-left corner of grid in graphics context
	private int yoffset = 0;
	
	// grid width, in number of cells
	private final int width;
	
	// height, in number of cells
	private final int height;
	
	// name of grid
	private String name;
	
	// description for grid
	private String description;
	
	// 2-dimensional array of Cells representing this grid
	private final Cell[][] cells;
	
	// currently selected cell, if any, else null
	private Cell currentCell;
	
	// row (y position) of currently selected cell, if any, else -1
	private int currentRow = -1;
	
	// column (x position) of currently selected cell, if any, else -1
	private int currentColumn = -1;
	
	// current direction that cursor is moving, as letters are typed -- North, South, East, or West
	private NsewDirection currentDirection = NsewDirection.NORTH;

	/**
	 * Constructor for Grid
	 * @param width width of grid, in number of cells
	 * @param height height of grid, in number of cells
	 * @param name short name for grid
	 * @param description longer description for grid
	 */
	public Grid(int width, int height, String name, String description)
	{
		this.width = width;
		this.height = height;
		this.name = name;
		this.description = description;
		cells = new Cell[this.height][this.width];
		for (int row = 0; row < height; ++row) {
			for (int col = 0; col < width; ++col) {
				cells[row][col] = new Cell();
			}
		}
	}
	
	/**
	 * Copy constructor, copies another Grid
	 * @param gridToCopy source grid to copy to this
	 */
	public Grid(Grid gridToCopy) {
		this(gridToCopy.getWidth(), gridToCopy.getHeight(), gridToCopy.getName(), gridToCopy.getDescription());
		for (int row = 0; row < height; ++row) {
			for (int col = 0; col < width; ++col) {
				getCell(row, col).copyFrom(gridToCopy.getCell(row, col));
			}
		}
		this.cellHeight = gridToCopy.cellHeight;
		this.cellWidth = gridToCopy.cellWidth;
		this.currentCell = gridToCopy.currentCell;
	}

	/**
	 * Return whether this grid is structurally equivalent to the specified grid, i.e., same dimensions, same pattern of blacks.
	 * @param other
	 * @return whether this grid is structurally equivalent to the specified grid, i.e., same dimensions, same pattern of blacks
	 */
	public boolean isStructureEqualTo(Grid other) {
		if (getWidth() != other.getWidth() || getHeight() != other.getHeight())
			return false;
		
		for (int row = 0; row < height; ++row)
			for (int col = 0; col < width; ++col)
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
		g.setColor(Color.BLACK);
		for (int row = 0; row < height; ++row) {
			for (int col = 0; col < width; ++col) {
				Cell cell = getCell(row, col);
				if (cell.isBlack()) {
					g.fillRect(xoffset + col * cellWidth, yoffset + row * cellHeight, cellWidth, cellHeight);
				} else {
					if (! thumbnail) {
						// Color currently selected cell light yellow, and color next cell to visit with a triangle indicator
						if (currentCell == cell) {
							g.setColor(LIGHT_YELLOW);
							g.fillRect(xoffset + col * cellWidth, yoffset + row * cellHeight, cellWidth, cellHeight);
							g.setColor(Color.BLACK);
						} else if (currentDirection == NsewDirection.NORTH && row < height-1 && getCell(row+1, col) == currentCell) {
							g.setColor(LIGHT_YELLOW);
							int x[] = new int[3];
							int y[] = new int[3];
							x[0] = xoffset + col * cellWidth;
							x[2] = xoffset + (col + 1) * cellWidth - 1;
							x[1] = (x[0] + x[2]) / 2;
							y[0] = yoffset + (row + 1) * cellHeight - 1;
							y[1] = y[0] - cellHeight / 2;
							y[2] = y[0];
							g.fillPolygon(x, y, 3);
							g.setColor(Color.BLACK);
						} else if (currentDirection == NsewDirection.SOUTH && row > 0 && getCell(row-1, col) == currentCell) {
							
						} else if (currentDirection == NsewDirection.EAST && col > 0 && getCell(row, col-1) == currentCell) {
							
						} else if (currentDirection == NsewDirection.WEST && col < width-1 && getCell(row, col+1) == currentCell) {
							
						}
						g.drawString(cell.getContents(), xoffset + col * cellWidth + cellWidthResidual / 2,
								yoffset + row * cellHeight + ascent + cellHeightResidual / 2);
						if (cell.getNumber() > 0) {
							g.setFont(smallFont);
							g.drawString(Integer.toString(cell.getNumber()), xoffset + col * cellWidth, yoffset + row * cellHeight + 8);
							g.setFont(font);
						}
					}
				}
				g.drawRect(xoffset + col * cellWidth, yoffset + row * cellHeight, cellWidth, cellHeight);
				
			}
		}
//		g.fillRect(0, 0, 30, 30);
	}

	/**
	 * Print grid to specitied PrintStream.
	 * @param stream PrintStream to print grid to
	 */
	public void printToStream(PrintStream stream) {
		StringBuilder temp = new StringBuilder();
		for (int row = 0; row < height; ++row) {
			for (int col = 0; col < width; ++col) {
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
	
	/**
	 * Regenerate numbers in all the cells by examining each cell to see which ones start words.
	 */
	public void renumberCells() {
		int currNumber = 1;
		
		for (int row = 0; row < height; ++row)
			for (int col = 0; col < width; ++col)
				cells[row][col].setNumber(isStartOfAcrossWord(row, col) || isStartOfDownWord(row, col) ? currNumber++ : 0);
	}

	/**
	 * Return whether the specified (row, col) starts an across word.
	 * It starts an across word if it is not black AND (leftmost on the line, or has a black to its left) AND non-black to the right.
	 * @param row 0-based row coordinate
	 * @param col 0-based column coordinate
	 * @return whether the specified (row, col) starts an across word
	 */
	public boolean isStartOfAcrossWord(int row, int col) {
		return ! cells[row][col].isBlack() && ((col == 0 || cells[row][col - 1].isBlack()) && (col < width - 1 && ! cells[row][col + 1].isBlack()));
	}

	/**
	 * Return whether the specified (row, col) starts a down word.
	 * It starts an down word if it is not black AND (it is topmost in its column, or has a black above it) AND and non-black below it.
	 * @param row 0-based row coordinate
	 * @param col 0-based column coordinate
	 * @return whether the specified (row, col) starts a down word
	 */
	public boolean isStartOfDownWord(int row, int col) {
		return ! cells[row][col].isBlack() && ((row == 0 || cells[row - 1][col].isBlack()) && (row < height - 1 && ! cells[row + 1][col].isBlack()));
	}

	/**
	 * @param row row
	 * @param col column
	 * @return cell based upon 0-based row and col
	 */
	public Cell getCell(int row, int col) {
		return cells[row][col];
	}
	
	/**
	 * Handle mouse clicked event.
	 * @param evt mouse clicked event from AWT
	 */
	public void mouseClicked(MouseEvent evt) {
		LOG.finest("mouse clicked at " + evt.getX() + ", " + evt.getY());
		int row = getRowForMouseYPosition(evt.getY());
		int column = getColumnForMouseXPosition(evt.getX());
		if (row != -1 && column != -1) {
			currentCell = getCell(row, column);
			currentRow = row;
			currentColumn = column;
			//Cell cell = getCellForXY(evt.getX(), evt.getY());
//			if (cell != null) {
//				currentCell = cell;
//				renumberCells();
//			}
		}
	}

	/**
	 * Handle key typed event.
	 * @param evt key typed event from AWT
	 */
	public void keyTyped(KeyEvent evt) {
		char c = evt.getKeyChar();
		if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
			c = Character.toUpperCase(c);
			if (currentCell != null && !currentCell.isBlack()) {
				currentCell.setContents(String.valueOf(c));
				advanceCursor();
				renumberCells();
			}
		}
	}
	
	public void toggleCurrentCell() {
		if (currentCell != null) {
			currentCell.toggleBlack();
		}
	}
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}
	
	/**
	 * @return the total number of black cells
	 */
	public int getNumberBlackCells() {
		int retval = 0;
		for (int row = 0; row < height; ++row)
			for (int col = 0; col < width; ++col)
				if (cells[row][col].isBlack())
					++retval;
		return retval;
	}
	
	/**
	 * @return the total number of across words + down words
	 */
	public int getNumberOfWords() {
		int retval = 0;
		for (int row = 0; row < height; ++row)
			for (int col = 0; col < width; ++col) {
				if (isStartOfAcrossWord(row, col))
					retval++;
				if (isStartOfDownWord(row, col))
					retval++;
			}
		return retval;
	}
	
	/**
	 * @return list of across words
	 */
	public List<GridWord> getAcrossWords() {
		int counter = 0;
		List<GridWord> retval = new ArrayList<GridWord>();
		for (int row = 0; row < height; ++row)
			for (int col = 0; col < width; ++col) {
				if (isStartOfAcrossWord(row, col)) {
					counter++;
					int colEnd = col;
					while (colEnd < width && ! getCell(row, colEnd).isBlack())
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
	
	/**
	 * @return list of down words
	 */
	public List<GridWord> getDownWords() {
		int counter = 0;
		List<GridWord> retval = new ArrayList<GridWord>();
		for (int row = 0; row < height; ++row)
			for (int col = 0; col < width; ++col) {
				if (isStartOfDownWord(row, col)) {
					counter++;
					int rowEnd = row;
					while (rowEnd < height && ! getCell(rowEnd, col).isBlack())
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	private Cell getCellForXY(int x, int y) {
		if (x < 0 || x > getCellWidth() * width || y < 0 || y > getCellHeight() * height)
			return null;
		int row = y / getCellHeight();
		int col = x / getCellWidth();
		return getCell(row, col);
	}
	
	// return grid column corresponding to the specified mouse click x position, if any, else -1
	private int getColumnForMouseXPosition(int x) {
		return x < xoffset || x > xoffset + cellWidth * width ? -1 : (x - xoffset) / cellWidth;
	}
	
	// return grid row corresponding to the specified mouse click y position, if any, else -1
	private int getRowForMouseYPosition(int y) {
		return y < yoffset || y > yoffset + cellHeight * height ? -1 : (y - yoffset) / cellHeight;
	}
	
	// move cursor one position in the currently-selected direction, if possible
	private void advanceCursor() {
		if (currentDirection == NsewDirection.NORTH && currentRow > 0) {
			--currentRow;
		} else if (currentDirection == NsewDirection.SOUTH && currentRow < height - 1) {
			++currentRow;
		} else if (currentDirection == NsewDirection.EAST && currentColumn < width - 1) {
			++currentColumn;
		} else if (currentDirection == NsewDirection.WEST && currentColumn > 0) {
			--currentColumn;
		}
		currentCell = getCell(currentRow, currentColumn);
	}
	
	private int getCellWidth()	{
		return cellWidth;
	}
	
	private int getCellHeight()	{
		return cellHeight;
	}

	public Cell getCurrentCell() {
		return currentCell;
	}

	public void setCurrentCell(Cell currentCell) {
		this.currentCell = currentCell;
	}

	public int getCurrentRow() {
		return currentRow;
	}

	public void setCurrentRow(int currentRow) {
		this.currentRow = currentRow;
	}

	public int getCurrentColumn() {
		return currentColumn;
	}

	public void setCurrentColumn(int currentColumn) {
		this.currentColumn = currentColumn;
	}

	/**
	 * @return the currentDirection
	 */
	public NsewDirection getCurrentDirection() {
		return currentDirection;
	}

	/**
	 * @param currentDirection the currentDirection to set
	 */
	public void setCurrentDirection(NsewDirection currentDirection) {
		this.currentDirection = currentDirection;
	}
	
}
