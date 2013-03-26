/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import us.stangl.crostex.AcrossDownDirection;
import us.stangl.crostex.ServiceException;

/**
 * Object reponsible for serializing to/from JPZ files.
 * @author Alex Stangl
 */
public class JpzSerializer {
	
	// element names
	private static final String RECTANGULAR_PUZZLE_ELEMENT_NAME = "rectangular-puzzle";
	private static final String METADATA_ELEMENT_NAME = "metadata";
	private static final String CROSSWORD_ELEMENT_NAME = "crossword";
	private static final String GRID_ELEMENT_NAME = "grid";
	private static final String TITLE_ELEMENT_NAME = "title";
	private static final String CREATOR_ELEMENT_NAME = "creator";
	private static final String COPYRIGHT_ELEMENT_NAME = "copyright";
	private static final String DESCRIPTION_ELEMENT_NAME = "description";
	private static final String CELL_ELEMENT_NAME = "cell";
	private static final String CLUES_ELEMENT_NAME = "clues";
	private static final String CLUES_ELEMENT_TITLE_NAME = "title";
	private static final String CLUE_ELEMENT_NAME = "clue";
	
	// attribute names
	private static final String GRID_WIDTH_ATTRIBUTE_NAME = "width";
	private static final String GRID_HEIGHT_ATTRIBUTE_NAME = "height";
	private static final String CELL_X_ATTRIBUTE_NAME = "x";
	private static final String CELL_Y_ATTRIBUTE_NAME = "y";
	private static final String CELL_NUMBER_ATTRIBUTE_NAME = "number";
	private static final String CELL_SOLUTION_ATTRIBUTE_NAME = "solution";
	private static final String CELL_TYPE_ATTRIBUTE_NAME = "type";
	private static final String CLUE_NUMBER_ATTRIBUTE_NAME = "number";
	
	/**
	 * Build grid from the specified JPZ file bytes.
	 * NOTE: no need to get bend over backwards to close streams here because it's all
	 * in-memory data that will be GC-ed anyhow -- no file handle resources to free.
	 * @param bytes bytes comprising JPZ file
	 * @param factory factory to use to create new grid
	 * @return new grid built from the specified JPZ file bytes
	 * @throws JpzSerializationException if bytes do not appear to be a valid JPZ file
	 */
	@SuppressWarnings("unchecked")
	public <T extends IoGrid> T fromBytes(byte[] bytes, IoGridFactory<T> factory) throws JpzSerializationException {
		try {
			ZipInputStream zipStream  = new ZipInputStream(new ByteArrayInputStream(bytes));
			
			// DOMSerializer cannot consume directly from zipStream because there are XML violations
			// (e.g., use of &nbsp; that we need to deal with that would otherwise cause failure)
			ZipEntry zipEntry = zipStream.getNextEntry();
			String entryName =  zipEntry.getName();
			if (! entryName.toUpperCase().endsWith(".JPZ"))
				throw new JpzSerializationException("Filename " + entryName + " within JPZ unexpectedly doesn't end with .JPZ");
			int size = (int)zipEntry.getSize();
			byte[] uncompressedBytes = new byte[size];

			int offset = 0;
			int nRead = 0;
			int remaining = size;
			while (remaining > 0 && nRead != -1) {
				nRead = zipStream.read(uncompressedBytes, offset, remaining);
				if (nRead > 0) {
					offset += nRead;
					remaining -= nRead;
				}
			}
			
			zipStream.closeEntry();
			zipStream.close();
			
			String jpzContents = new String(uncompressedBytes, "UTF-8");
			if (jpzContents.contains("<!DOCTYPE"))
				throw new JpzSerializationException("JPZ already contains a DOCTYPE declaration");
			
			String rootTag = "<crossword-compiler-applet";
			int rootTagIndex = jpzContents.indexOf(rootTag);
			if (rootTagIndex == -1)
				throw new JpzSerializationException("Root tag " + rootTag + " not found while parsing JPZ");
			
			String revisedString = jpzContents.substring(0, rootTagIndex) + "\r\n<!DOCTYPE jpz [<!ENTITY nbsp \"&#160;\">]>\r\n" + jpzContents.substring(rootTagIndex);
			Document document = DOMSerializer.readDocument(new ByteArrayInputStream(revisedString.getBytes("UTF-8")));
			
			NodeList nodeList = document.getElementsByTagName(RECTANGULAR_PUZZLE_ELEMENT_NAME);
			if (nodeList.getLength() != 1)
				throw new JpzSerializationException("Expected exactly 1 element with name " + RECTANGULAR_PUZZLE_ELEMENT_NAME
						+ " but found " + Integer.toString(nodeList.getLength()));
			Element rectangularPuzzleElement = (Element)nodeList.item(0);
			Element metadataElement = getOptionalElement(rectangularPuzzleElement, METADATA_ELEMENT_NAME);
			Element crosswordElement = getRequiredElement(rectangularPuzzleElement, CROSSWORD_ELEMENT_NAME);
			Element gridElement = getRequiredElement(crosswordElement, GRID_ELEMENT_NAME);
			int width = Integer.valueOf(gridElement.getAttribute(GRID_WIDTH_ATTRIBUTE_NAME));
			int height = Integer.valueOf(gridElement.getAttribute(GRID_HEIGHT_ATTRIBUTE_NAME));
			
			T grid = factory.newGrid(width, height);
			if (metadataElement != null) {
				grid.setTitle(getOptionalStringElement(metadataElement, TITLE_ELEMENT_NAME, ""));
				grid.setAuthor(getOptionalStringElement(metadataElement, CREATOR_ELEMENT_NAME, ""));
				grid.setCopyright(getOptionalStringElement(metadataElement, COPYRIGHT_ELEMENT_NAME, ""));
				grid.setNotes(getOptionalStringElement(metadataElement, DESCRIPTION_ELEMENT_NAME, ""));
			}
			
			NodeList cellList = gridElement.getElementsByTagName(CELL_ELEMENT_NAME);
			int nCells = cellList.getLength();
			for (int cellIndex = 0; cellIndex < nCells; ++cellIndex) {
				Element cellElement = (Element)cellList.item(cellIndex);
				int column = parseCellRange(cellElement.getAttribute(CELL_X_ATTRIBUTE_NAME)) - 1;
				int row = parseCellRange(cellElement.getAttribute(CELL_Y_ATTRIBUTE_NAME)) - 1;
				String cellNumber = cellElement.getAttribute(CELL_NUMBER_ATTRIBUTE_NAME);
				String cellContents = cellElement.getAttribute(CELL_SOLUTION_ATTRIBUTE_NAME);
				String cellType = cellElement.getAttribute(CELL_TYPE_ATTRIBUTE_NAME);
				if (cellType != null && cellType.equals("block")) {
					grid.setBlackCell(row, column, true);
				} else if (cellContents.length() > 0) {
					grid.setCellContents(row, column, cellContents);
				}
			}
			NodeList cluesList = crosswordElement.getElementsByTagName(CLUES_ELEMENT_NAME);
			int cluesListSize = cluesList.getLength();
			if (cluesListSize != 2)
				throw new JpzSerializationException("Expected 2 clues lists, but found "
					+ Integer.toString(cluesListSize));
			for (int i = 0; i < cluesListSize; ++i) {
				Element cluesElement = (Element)cluesList.item(i);
				Element titleElement = getRequiredElement(cluesElement, CLUES_ELEMENT_TITLE_NAME);
				AcrossDownDirection direction = directionStringToDirection(titleElement.getTextContent());
				if (direction == AcrossDownDirection.ACROSS) {
					grid.setAcrossClues(parseClues(cluesElement, direction, grid));
				} else if (direction == AcrossDownDirection.DOWN) {
					grid.setDownClues(parseClues(cluesElement, direction, grid));
				} else {
					throw new JpzSerializationException("Unhandled direction " + direction);
				}
				String titleText = titleElement.getTextContent();
				System.out.println("titleText = " + titleText);
			}
			
			return grid;
		} catch (NumberFormatException e) {
			throw new JpzSerializationException("NumberFormatException unexpectedly caught while parsing JPZ", e);
		} catch (ServiceException e) {
			throw new JpzSerializationException("ServiceException unexpectedly caught while parsing JPZ", e);
		} catch (IOException e) {
			throw new JpzSerializationException("IOException unexpectedly caught while parsing JPZ", e);
		}
	}

	/**
	 * Serialize IoGrid to JPZ UTF-8 byte stream.
	 * @param grid
	 * @return
	 */
	public byte[] toBytes(IoGrid grid) throws JpzSerializationException {

		//TODO implement this
		return null;
	}
	
	private Element getOptionalElement(Element parentElement, String elementName) throws JpzSerializationException {
		NodeList nodeList = parentElement.getElementsByTagName(elementName);
		int len = nodeList.getLength();
		if (len == 0)
			return null;
		if (len != 1)
			throw new JpzSerializationException("Expected at most 1 element with name " + elementName
					+ " but found " + Integer.toString(len));
		return (Element)nodeList.item(0);
	}
	private Element getRequiredElement(Element parentElement, String elementName) throws JpzSerializationException {
		NodeList nodeList = parentElement.getElementsByTagName(elementName);
		int len = nodeList.getLength();
		if (len != 1)
			throw new JpzSerializationException("Expected exactly 1 element with name " + elementName
					+ " but found " + Integer.toString(len));
		return (Element)nodeList.item(0);
	}
	
	// get optional string element, returning defaultValue if missing
	private String getOptionalStringElement(Element parentElement, String elementName, String defaultValue) throws JpzSerializationException {
		NodeList nodeList = parentElement.getElementsByTagName(elementName);
		int len = nodeList.getLength();
		if (len == 0)
			return defaultValue;
		if (len != 1)
			throw new JpzSerializationException("Expected at most 1 element with name " + elementName
					+ " but found " + Integer.toString(len));
		Element element = (Element)nodeList.item(0);
		String retval = element.getTextContent();
		return retval == null ? defaultValue : retval;
	}
	
	private int parseCellRange(String cellRange) throws JpzSerializationException {
		if (cellRange == null)
			throw new JpzSerializationException("null cell range");
		String trimmedRange = cellRange.trim();
		if (trimmedRange.charAt(0) == '-')
			throw new JpzSerializationException("negative cell range");
		if (trimmedRange.indexOf('-') != -1)
			throw new JpzSerializationException("cell range not currently supported: " + trimmedRange);
		try {
			return Integer.valueOf(trimmedRange);
		} catch (NumberFormatException e) { 
			throw new JpzSerializationException("NumberFormatException caught parsing cell range " + trimmedRange, e);
		}
	}
	
	private List<IoClue> parseClues(Element cluesContainerElement, AcrossDownDirection clueDirection, IoGrid grid) {
		List<IoClue> retval = new ArrayList<IoClue>();
		NodeList clueList = cluesContainerElement.getElementsByTagName(CLUE_ELEMENT_NAME);
		int nClues = clueList.getLength();
		for (int i = 0; i < nClues; ++i) {
			Element clueElement = (Element)clueList.item(i);
			int clueNumber = Integer.parseInt(clueElement.getAttribute(CLUE_NUMBER_ATTRIBUTE_NAME));
			retval.add(newIoClue(grid, clueNumber, clueElement.getTextContent(), clueDirection));
		}
		return retval;
	}
	
	private IoClue newIoClue(IoGrid grid, int clueNumber, String clueText, AcrossDownDirection clueDirection) {
		IoClue clue = grid.newClue();
		clue.setNumber(clueNumber);
		clue.setClueText(clueText);
		clue.setDirection(clueDirection);
		return clue;
	}
	
	// map string containing Across or Down to the corresponding enum value
	private AcrossDownDirection directionStringToDirection(String direction) throws JpzSerializationException {
		String upDirection = direction.toUpperCase();
		if (upDirection.contains("ACROSS"))
			return AcrossDownDirection.ACROSS;
		if (upDirection.contains("DOWN"))
			return AcrossDownDirection.DOWN;
		throw new JpzSerializationException("Unrecognized direction " + direction);
	}
}
