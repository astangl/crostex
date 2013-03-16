/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import us.stangl.crostex.AcrossDownDirection;
import us.stangl.crostex.util.Pair;

/**
 * Object used to serialize a Grid to/from Across Lite .PUZ file format.
 * Based upon the PUZ detailed file format specification from http://code.google.com/p/puz/wiki/FileFormat
 * @author Alex Stangl
 */
public class PuzSerializer {
	// logger
	private static final Logger LOG = Logger.getLogger(PuzSerializer.class.getName());
	
	// File magic string
	private static final String FILE_MAGIC_STRING = "ACROSS&DOWN\0";
	
	// Version string
	private static final String VERSION_STRING = "1.3\0";
	
	// Character set to use for byte array <-> String conversions
	private static final String CHARACTER_SET = "ISO-8859-1";

	// Offsets of various fields within the PUZ data block
	private static final int OFFSET_OVERALL_CHKSUM = 0x00;
	private static final int OFFSET_FILE_MAGIC = 0x02;
	private static final int OFFSET_CIB_CHKSUM = 0x0e;
	private static final int OFFSET_ENCRYPTED_CHKSUMS = 0x10;
	private static final int OFFSET_VERSION_STRING = 0x18;
	private static final int OFFSET_WIDTH = 0x2c;
	private static final int OFFSET_HEIGHT = 0x2d;
	private static final int OFFSET_NBR_CLUES = 0x2e;
	private static final int OFFSET_SOLUTION = 0x34;
	
	// contents of a solution cell that represents black
	private static final byte BLACK_SOLUTION_CELL = 0x2e;

	/**
	 * @return whether the specified grid is in a state ready to export
	 */
	public boolean isReadyToExportToPuz(IoGrid grid) {
		Pair<String, String> solutionAndPlayerState = getSolutionAndPlayerState(grid);
		return solutionAndPlayerState != null;
	}

	/**
	 * Build grid from the specified PUZ file bytes.
	 * @param bytes bytes comprising PUZ file
	 * @param factory factory to use to create new grid
	 * @return new grid built from the specified PUZ file bytes, or null if bytes don't form a valid PUZ
	 * @throws IllegalArgumentException if bytes do not appear to be a valid PUZ file
	 */
	public <T extends IoGrid> T fromBytes(byte[] bytes, IoGridFactory<T> factory, boolean enforceChecksums) {
		//TODO allow for PUZ files with preamble before the global checksum and file magic. Requires searching for magic string.
		if (! Arrays.equals(toByteArray(FILE_MAGIC_STRING), Arrays.copyOfRange(bytes, OFFSET_FILE_MAGIC, 0x0e)))
			throw new IllegalArgumentException("Not a valid PUZ byte stream: FILE MAGIC mismatch");

		int width = bytes[OFFSET_WIDTH] & 0xff;
		if (width <= 0)
			throw new IllegalArgumentException("Not a valid PUZ byte stream: width = " + width);
		
		int height = bytes[OFFSET_HEIGHT] & 0xff;
		if (height <= 0)
			throw new IllegalArgumentException("Not a valid PUZ byte stream: height = " + height);

		int nbrClues = getShortAt(bytes, OFFSET_NBR_CLUES);
		if (nbrClues <= 0)
			throw new IllegalArgumentException("Not a valid PUZ byte stream: nbrClues = " + nbrClues);

		T retval = factory.newGrid(width, height);
		PuzChecksums checksums = new PuzChecksums(bytes, 0);
		if (checksums.cksum != getShortAt(bytes, OFFSET_OVERALL_CHKSUM)) {
			LOG.warning("Global checksum mismatch in fromBytes, computed = " + checksums.cksum + " versus stored " + getShortAt(bytes, OFFSET_OVERALL_CHKSUM));
			if (enforceChecksums)
				return null;
		}
		if (checksums.c_cib != getShortAt(bytes, OFFSET_CIB_CHKSUM)) {
			LOG.warning("CIB checksum mismatch in fromBytes");
			if (enforceChecksums)
				return null;
		}
		for (int i = 0; i < 8; ++i) {
			if (checksums.encrypted_chksums[i] != bytes[OFFSET_ENCRYPTED_CHKSUMS + i]) {
				LOG.warning("Encrypted checksum mismatch in fromBytes at i = " + i + ", computed = " + checksums.encrypted_chksums[i] + " versus stored " + bytes[OFFSET_ENCRYPTED_CHKSUMS + i]);
				if (enforceChecksums)
					return null;
			}
		}
		int solutionIndex = OFFSET_SOLUTION;
		for (int row = 0; row < height; ++row) {
			for (int column = 0; column < width; ++ column) {
				byte b = bytes[solutionIndex];
				if (b == BLACK_SOLUTION_CELL)
					retval.setBlackCell(row, column, true);
				else
					retval.setCellContents(row, column, fromByteArray(bytes, solutionIndex, 1));
				++solutionIndex;
			}
		}
		
		int nCells = width * height;
		
		int offsetStrings = OFFSET_SOLUTION + nCells + nCells;
		int nbrStrings = 4 + nbrClues;
		String[] strings = new String[nbrStrings];
		for (int i = 0; i < nbrStrings; ++i) {
			int strlen = strLength(bytes, offsetStrings);
			strings[i] = fromByteArray(bytes, offsetStrings, strlen);
			offsetStrings = offsetStrings + strlen + 1;
		}
		
		retval.setTitle(strings[0]);
		retval.setAuthor(strings[1]);
		retval.setCopyright(strings[2]);
		
		//TODO finish implementing this
		return retval;
	}
	
	public byte[] toPuz(IoGrid grid) {
		int width = grid.getWidth();
		int height = grid.getHeight();
		int nCells = width * height;
		// get all the clues in the correct sorted order
		List<? extends IoClue> acrossClues = grid.getAcrossClues();
		List<? extends IoClue> downClues = grid.getDownClues();
		int nbrClues = acrossClues.size() + downClues.size();
		List<IoClue> allClues = new ArrayList<IoClue>(nbrClues);
		allClues.addAll(acrossClues);
		allClues.addAll(downClues);
		Collections.sort(allClues, new ClueComparator());
		String title = grid.getTitle();
		String author = grid.getAuthor();
		String copyright = grid.getCopyright();
		String notes = grid.getNotes();
		List<String> strings = new ArrayList<String>(nbrClues + 4);
		strings.add(title);
		strings.add(author);
		strings.add(copyright);
		
		Pair<String, String> solutionAndPlayerState = getSolutionAndPlayerState(grid);
		String solution = solutionAndPlayerState.first;
		String playerState = solutionAndPlayerState.second;
		if (solution.length() != nCells)
			throw new RuntimeException("solution '" + solution + "' not expected length " + nCells);
		if (playerState.length() != nCells)
			throw new RuntimeException("playerState '" + playerState + "' not expected length " + nCells);

		// ...and strings
		for (IoClue clue : allClues)
			strings.add(clue.getClueText());
		
		strings.add(notes);
		
		// initialize total strings length to # of strings since each one has an extra null character
		int totalStringsLength = strings.size();
		for (String string : strings)
			totalStringsLength += string.length();

		// Allocate full byte array, not taking into account any extra sections (that may come later), then populate it
		int totalLength = 52 + nCells + nCells + totalStringsLength;
		byte[] mainPuz = new byte[totalLength];
		mainPuz[OFFSET_WIDTH] = (byte)width;
		mainPuz[OFFSET_HEIGHT] = (byte)height;
		putShortAt((short)nbrClues, mainPuz, OFFSET_NBR_CLUES);
		System.arraycopy(toByteArray(FILE_MAGIC_STRING), 0, mainPuz, OFFSET_FILE_MAGIC, 12);
		System.arraycopy(toByteArray(VERSION_STRING), 0, mainPuz, OFFSET_VERSION_STRING, 4);
		System.arraycopy(toByteArray(solution), 0, mainPuz, OFFSET_SOLUTION, nCells);
		System.arraycopy(toByteArray(playerState), 0, mainPuz, OFFSET_SOLUTION + nCells, nCells);
		
		int index = OFFSET_SOLUTION + nCells + nCells;
		for (String string : strings) {
			byte[] stringBytes = toByteArray(string + "\0");
			System.arraycopy(stringBytes, 0, mainPuz, index, stringBytes.length);
			index += stringBytes.length;
		}

		// Now compute and add in checksums
		PuzChecksums checksums = new PuzChecksums(mainPuz, 0);
		System.arraycopy(checksums.encrypted_chksums, 0, mainPuz, OFFSET_ENCRYPTED_CHKSUMS, 8);
		putShortAt(checksums.cksum, mainPuz, OFFSET_OVERALL_CHKSUM);
		putShortAt(checksums.c_cib, mainPuz, OFFSET_CIB_CHKSUM);
		return mainPuz;
	}
	
	// return solution and player state for grid, if possible, else null
	private Pair<String, String> getSolutionAndPlayerState(IoGrid grid) {
		int width = grid.getWidth();
		int height = grid.getHeight();
		int nCells = width * height;
		
		StringBuilder solutionBuilder = new StringBuilder(nCells);
		StringBuilder playerStringBuilder = new StringBuilder(nCells);
		for (int row = 0; row < height; ++row) {
			for (int column = 0; column < width; ++column) {
				if (grid.isBlackCell(row, column)) {
					solutionBuilder.append('.');
					playerStringBuilder.append('.');
				} else {
					// TODO what to do if cell not black, but doesn't have contents either?
					String cellContents = grid.getCellContents(row, column);
					if (cellContents == null || cellContents.length() == 0)
						return null;
					solutionBuilder.append(cellContents.charAt(0));
					playerStringBuilder.append('-');
				}
			}
		}
		return new Pair<String, String>(solutionBuilder.toString(), playerStringBuilder.toString());
	}
	
	// convert string to byte array using ISO-8859-1 encoding
	private byte[] toByteArray(String string) {
		try {
			return string.getBytes(CHARACTER_SET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException unexpectedly caught, trying to getBytes for '" + string + "'.", e);
		}
	}
	
	// convert byte array to String using ISO-8859-1 encoding
	private String fromByteArray(byte[] bytes) {
		try {
			return new String(bytes, CHARACTER_SET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException unexpectedly caught, trying fromBytes for '" + bytes + "'.", e);
		}
	}

	// convert byte array to String using ISO-8859-1 encoding
	private String fromByteArray(byte[] bytes, int index, int length) {
		try {
			return new String(bytes, index, length, CHARACTER_SET);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException unexpectedly caught, trying fromBytes for '" + bytes + "'.", e);
		}
	}

	// store short into byte array at specified offset, in little-endian order
	private static void putShortAt(short value, byte[] bytes, int offset) {
		bytes[offset] = (byte)value;
		bytes[offset + 1] = (byte)(value >> 8);
	}

	// retrieve short from byte array at specified offset, in little-endian order
	private static short getShortAt(byte[] bytes, int offset) {
		return (short)((bytes[offset] & 0xff) + 256 * (bytes[offset + 1] & 0xff));
	}


	// return length of null-terminated string starting at region[index], not including null terminator
	private static int strLength(byte[] region, int index) {
		int len = 0;
		while (region[index + len] != 0)
			++len;
		return len;
	}
	
	// comparator to sort Clues in order, first by number, then by Across before Down (for identical numbers)
	private static final class ClueComparator implements Comparator<IoClue> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(IoClue clue1, IoClue clue2) {
			int clueNumber1 = clue1.getNumber();
			int clueNumber2 = clue2.getNumber();
			if (clueNumber1 < clueNumber2)
				return -1;
			if (clueNumber1 > clueNumber2)
				return 1;
			AcrossDownDirection direction1 = clue1.getDirection();
			AcrossDownDirection direction2 = clue2.getDirection();
			
			// we should never have to return 0, as we should never be comparing to the same clue
			// so we will treat that as an error condition
			if (direction1 == direction2)
				throw new RuntimeException("Unexpectedly found duplicate clue in ClueComparator: " + clue1 + ", " + clue2);
			return direction1 == AcrossDownDirection.ACROSS ? -1 : 1;
		}
		
	}
	private static final class PuzChecksums {
		public final short c_cib;
		public final short cksum;
		public final byte[] encrypted_chksums = new byte[8];
		//
		public PuzChecksums(byte[] puzData, int startOffset) {
			int width = puzData[startOffset + OFFSET_WIDTH] & 0xff;
			int height = puzData[startOffset + OFFSET_HEIGHT] & 0xff;
			int nCells = width * height;
			int nbrClues = getShortAt(puzData, startOffset + OFFSET_NBR_CLUES);
			c_cib = cksum_region(puzData, startOffset + OFFSET_WIDTH, 8, (short)0);
			short cksum = c_cib;
			short c_part = (short)0;
			
			short c_sol = cksum_region(puzData, startOffset + OFFSET_SOLUTION, nCells, (short)0);
			cksum = cksum_region(puzData, startOffset + OFFSET_SOLUTION, nCells, cksum);
			
			short c_grid = cksum_region(puzData, startOffset + OFFSET_SOLUTION + nCells, nCells, (short)0);
			cksum = cksum_region(puzData, startOffset + OFFSET_SOLUTION + nCells, nCells, cksum);
			
			int currOffset = startOffset + OFFSET_SOLUTION + nCells + nCells;

			int nbrStrings = nbrClues + 4;
			for (int i = 0; i < nbrStrings; ++i) {
				// only include null in checksum for title, author, copyright, and notes
				boolean includeNullInChecksum = i < 3 || i == nbrStrings - 1;
				int strLen = strLength(puzData, currOffset);
				int nextOffset = currOffset + strLen + 1;
				if (includeNullInChecksum)
					++strLen;
				// If it's one of the ones including null in checksum, only include in checksum for non-empty strings
				if (!includeNullInChecksum || strLen > 1) {
					c_part = cksum_region(puzData, currOffset, strLen, c_part);
					cksum = cksum_region(puzData, currOffset, strLen, cksum);
				}
				currOffset = nextOffset;
			}
			this.cksum = cksum;
			encrypted_chksums[0] = (byte)(0x49 ^ c_cib);
			encrypted_chksums[1] = (byte)(0x43 ^ c_sol);
			encrypted_chksums[2] = (byte)(0x48 ^ c_grid);
			encrypted_chksums[3] = (byte)(0x45 ^ c_part);
			encrypted_chksums[4] = (byte)(0x41 ^ (c_cib >> 8));
			encrypted_chksums[5] = (byte)(0x54 ^ (c_sol >> 8));
			encrypted_chksums[6] = (byte)(0x45 ^ (c_grid >> 8));
			encrypted_chksums[7] = (byte)(0x44 ^ (c_part >> 8));
		}

		// return checksum of len bytes starting at region[index], and initial checksum value cksum
		private short cksum_region(byte[] region, int index, int len, short cksum) {
			// use int to accumulate actual checksum so we don't have to worry about sign issues
			int intSum = cksum & 0xffff;
			for (int i = 0; i < len; ++i) {
				boolean lowBit = intSum % 2 == 1;
				intSum >>= 1;
				if (lowBit)
					intSum += 0x8000;
				intSum = (intSum + (region[index + i] & 0xff)) & 0xffff;
			}
			return (short)intSum;
		}
	}
}
