/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import us.stangl.crostex.AcrossDownDirection;

/**
 * Object reponsible for serializing to/from IPUZ files.
 * @author Alex Stangl
 */
public class IpuzSerializer {
	// header expected at front of IPUZ file, before JSON data
	private static final String IPUZ_HEADER = "ipuz(";
	
	// trailer expected after JSON data
	private static final String IPUZ_TRAILER = ")";
	
	// field names
	private static final String FIELD_VERSION = "version";
	private static final String FIELD_COPYRIGHT = "copyright";
	private static final String FIELD_KIND = "kind";
	private static final String FIELD_AUTHOR = "author";
	private static final String FIELD_NOTES = "notes";
	private static final String FIELD_DIMENSIONS = "dimensions";
	private static final String FIELD_PUZZLE = "puzzle";
	private static final String FIELD_SOLUTION = "solution";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_CLUES = "clues";
	private static final String FIELD_BLOCK = "block";
	private static final String FIELD_EMPTY = "empty";
	
	private static final String FIELD_WIDTH = "width";
	private static final String FIELD_HEIGHT = "height";
	
	private static final String FIELD_ACROSS_CLUES = "Across";
	private static final String FIELD_DOWN_CLUES = "Down";
	
	// supported kind
	private static final String EXPECTED_KIND = "http://ipuz.org/crossword#1";
	
	// default value for block field (representing black cell) and empty field
	private static final String FIELD_BLOCK_DEFAULT = "#";
	private static final String FIELD_EMPTY_DEFAULT = "0";
	
	// value used to represent a blocked (black) cell
	private String blockValue = FIELD_BLOCK_DEFAULT;
	private String emptyValue = FIELD_EMPTY_DEFAULT;
	
	/**
	 * Build grid from the specified IPUZ file bytes.
	 * @param bytes bytes comprising IPUZ file
	 * @param factory factory to use to create new grid
	 * @return new grid built from the specified IPUZ file bytes, or null if bytes don't form a valid IPUZ
	 * @throws IpuzParsingException if bytes do not appear to be a valid IPUZ file
	 */
	@SuppressWarnings("unchecked")
	public <T extends IoGrid> T fromBytes(byte[] bytes, IoGridFactory<T> factory) throws IpuzParsingException {

		try {
			String ipuzString = new String(bytes, "UTF-8").trim();
			if (! ipuzString.startsWith(IPUZ_HEADER))
				throw new IpuzParsingException("IPUZ data '" + ipuzString + "' doesn't start with " + IPUZ_HEADER);
			if (! ipuzString.endsWith(IPUZ_TRAILER))
				throw new IpuzParsingException("IPUZ data '" + ipuzString + "' doesn't end with " + IPUZ_TRAILER);
			String jsonString = ipuzString.substring(IPUZ_HEADER.length(), ipuzString.length() - IPUZ_TRAILER.length());
			Object obj = new JsonParser().parseJsonString(jsonString);
			if (! (obj instanceof Map))
				throw new IpuzParsingException("IPUZ JSON didn't decode into an object");
			Map<String,?> map = (Map<String,?>)obj;
			
			List<String> kinds = (List<String>)map.get(FIELD_KIND);
			//String kind = (String)map.get(FIELD_KIND);
			if (! kinds.contains(EXPECTED_KIND))
				throw new IpuzParsingException("IPUZ kinds '" + kinds + "' doesn't match expected kind '" + EXPECTED_KIND + "'");
			Map<String, ?> dimensions = (Map<String,?>)getRequiredField(map, FIELD_DIMENSIONS);
			Map<String, List<?>> clues = (Map<String, List<?>>)getRequiredField(map, FIELD_CLUES);
			List<?> acrossClues = getRequiredField(clues, FIELD_ACROSS_CLUES);
			List<?> downClues = getRequiredField(clues, FIELD_DOWN_CLUES);
			List<List<?>> puzzleRows = (List<List<?>>)getRequiredField(map, FIELD_PUZZLE);
			
			Double widthDouble = (Double)getRequiredField(dimensions, FIELD_WIDTH);
			Double heightDouble = (Double)getRequiredField(dimensions, FIELD_HEIGHT);
			
			// get value corresponding to a block, defaulting to #
			blockValue = (String)getOptionalField(map, FIELD_BLOCK, FIELD_BLOCK_DEFAULT);
			emptyValue = (String)getOptionalField(map, FIELD_EMPTY, FIELD_EMPTY_DEFAULT);
			
			int width = widthDouble.intValue();
			int height = heightDouble.intValue();
			
			T grid = factory.newGrid(width, height);
			grid.setTitle((String)getOptionalField(map, FIELD_TITLE, ""));
			grid.setAuthor((String)getOptionalField(map, FIELD_AUTHOR, ""));
			grid.setCopyright((String)getOptionalField(map, FIELD_COPYRIGHT, ""));
			grid.setNotes((String)getOptionalField(map, FIELD_NOTES, ""));

			List<List<?>> solution = (List<List<?>>)getRequiredField(map, FIELD_SOLUTION);
			if (solution.size() != height)
				throw new IpuzParsingException("Solution height " + solution.size() + " does not match puzzle height " + height);
			for (int row = 0; row < height; ++row) {
				List<?> puzzleList = puzzleRows.get(row);
				if (puzzleList.size() != width)
					throw new IpuzParsingException("Puzzle list row " + row + " width " + puzzleList.size() + " does not match puzzle width " + width);
				List<?> solutionList = solution.get(row);
				if (solutionList.size() != width)
					throw new IpuzParsingException("Solution list row " + row + " width " + solutionList.size() + " does not match puzzle width " + width);
				for (int column = 0; column < width; ++column) {
					Object solutionObject = solutionList.get(column);
					if (solutionObject instanceof String) {
						String string = (String)solutionObject;
						if (string.equals(blockValue))
							grid.setBlackCell(row, column, true);
						else if (! string.equals(emptyValue))
							grid.setCellContents(row, column, string);
					}
					//TODO handle other solution object types here
					Object puzzleObject = puzzleList.get(column);
					if (puzzleObject instanceof Map) {
						Map<String, ?> puzzleMap = (Map<String, ?>)puzzleObject;
						Object object = puzzleMap.get("cell");
						if (object != null)
							processLabelledCellValue(object, row, column, grid);
						Map<String, ?> styleSpec = (Map<String, ?>)puzzleMap.get("style");
						if (styleSpec != null) {
							String backgroundShape = (String)styleSpec.get("shapebg");
							if (backgroundShape != null && backgroundShape.equals("circle")) {
								grid.setCircledCell(row, column, true);
							}
						}
					} else {
						processLabelledCellValue(puzzleObject, row, column, grid);
					}
				}
			}
			
			grid.setAcrossClues(getCluesForDirection(acrossClues, AcrossDownDirection.ACROSS, grid));
			grid.setDownClues(getCluesForDirection(downClues, AcrossDownDirection.DOWN, grid));
			
			//TODO complete this
			return grid;
		} catch (UnsupportedEncodingException e) {
			throw new IpuzParsingException("UnsupportedEncodingException caught trying to parse IPUZ bytes as UTF-8", e);
		} catch (JsonParsingException e) {
			throw new IpuzParsingException("JsonParsingException caught trying to parse IPUZ bytes", e);
		}
	}
	
	// get required field from map, throwing IpuzParsingException if it is missing
	private <T> T getRequiredField(Map<String,T> map, String fieldName) throws IpuzParsingException {
		T retval = map.get(fieldName);
		if (retval == null)
			throw new IpuzParsingException("IPUZ data missing required field " + fieldName);
		return retval;
	}
	
	// get field from map, returning default value if it is missing
	private Object getOptionalField(Map<String,?> map, String fieldName, Object defaultValue) {
		Object retval = map.get(fieldName);
		return retval == null ? defaultValue : retval;
	}
	
	private List<IoClue> getCluesForDirection(List<?> clues,
			AcrossDownDirection clueDirection, IoGrid grid) throws IpuzParsingException
	{
		List<IoClue> retval = new ArrayList<IoClue>();
		for (Object object : clues) {
			if (object instanceof List) {
				List<?> list = (List<?>)object;
				if (list.size() != 2)
					throw new IpuzParsingException("Unexpected clue list size of " + list.size() + " versus expected 2");
				Double clueNumber = (Double)list.get(0);
				String clueText = (String)list.get(1);
				retval.add(newIoClue(grid, clueNumber.intValue(), clueText, clueDirection));
			}
			//TODO handle other types here
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
	
	private void processLabelledCellValue(Object value, int row, int column, IoGrid grid) {
		if (value instanceof String) {
			String string = (String)value;
			if (string.equals(blockValue))
				grid.setBlackCell(row, column, true);
		}
	}
}
