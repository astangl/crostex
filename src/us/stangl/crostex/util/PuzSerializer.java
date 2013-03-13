/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import us.stangl.crostex.AcrossDownDirection;
import us.stangl.crostex.Cell;
import us.stangl.crostex.Clue;
import us.stangl.crostex.Grid;

/**
 * Object used to serialize a Grid to/from Across Lite .PUZ file format.
 * Based upon the PUZ detailed file format specification from http://code.google.com/p/puz/wiki/FileFormat
 * @author Alex Stangl
 */
public class PuzSerializer {
	
	// File magic string
	private static final String FILE_MAGIC_STRING = "ACROSS&DOWN\0";
	
	// Version string
	private static final String VERSION_STRING = "1.3\0";

	/**
	 * @return whether the specified grid is in a state ready to export
	 */
	public boolean isReadyToExportToPuz(Grid grid) {
		Pair<String, String> solutionAndPlayerState = getSolutionAndPlayerState(grid);
		return solutionAndPlayerState != null;
	}

	public byte[] toPuz(Grid grid) {
		int width = grid.getWidth();
		int height = grid.getHeight();
		int nCells = width * height;
		// get all the clues in the correct sorted order
		List<Clue> acrossClues = grid.validateAndGetAcrossClues();
		List<Clue> downClues = grid.validateAndGetDownClues();
		int nbrClues = acrossClues.size() + downClues.size();
		List<Clue> allClues = new ArrayList<Clue>(nbrClues);
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

		// build CIB; cib[4]...cib[7] are null for now, so they don't need explicit initialization
		byte[] cib = new byte[8];
		cib[0] = (byte)width;
		cib[1] = (byte)height;
		cib[2] = (byte)nbrClues;
		cib[3] = (byte)(nbrClues / 256);
		
		
		// calculate some of the checksums
		short c_cib = cksum_region(cib, 0, 8, (short)0);
		short cksum = c_cib;
		short c_part = (short)0;
		
		byte[] solutionBytes = toByteArray(solution);
		short c_sol = cksum_region(solutionBytes, 0, nCells, (short)0);
		cksum = cksum_region(solutionBytes, 0, nCells, cksum);
		
		byte[] playerStateBytes = toByteArray(playerState);
		short c_grid = cksum_region(playerStateBytes, 0, nCells, (short)0);
		cksum = cksum_region(playerStateBytes, 0, nCells, cksum);
		
		if (title.length() > 0) {
			byte[] titleBytes = toByteArray(title + "\0");
			c_part = cksum_region(titleBytes, 0, titleBytes.length, c_part);
			cksum = cksum_region(titleBytes, 0, titleBytes.length, cksum);
		}
		
		if (author.length() > 0) {
			byte[] authorBytes = toByteArray(author + "\0");
			c_part = cksum_region(authorBytes, 0, authorBytes.length, c_part);
			cksum = cksum_region(authorBytes, 0, authorBytes.length, cksum);
		}
		if (copyright.length() > 0) {
			byte[] copyrightBytes = toByteArray(copyright + "\0");
			c_part = cksum_region(copyrightBytes, 0, copyrightBytes.length, c_part);
			cksum = cksum_region(copyrightBytes, 0, copyrightBytes.length, cksum);
		}
		// ...and strings
		for (Clue clue : allClues) {
			String clueText = clue.getClueText();
			strings.add(clueText);
			byte[] clueBytes = toByteArray(clueText);
			c_part = cksum_region(clueBytes, 0, clueBytes.length, c_part);
			cksum = cksum_region(clueBytes, 0, clueBytes.length, cksum);
		}
		if (notes.length() > 0) {
			byte[] notesBytes = toByteArray(notes + "\0");
			c_part = cksum_region(notesBytes, 0, notesBytes.length, c_part);
			cksum = cksum_region(notesBytes, 0, notesBytes.length, cksum);
		}
		
		strings.add(notes);
		
		// initialize total strings length to # of strings since each one has an extra null character
		int totalStringsLength = strings.size();
		for (String string : strings)
			totalStringsLength += string.length();

		// Allocate full byte array, not taking into account any extra sections (that may come later), then populate it
		int totalLength = 52 + nCells + nCells + totalStringsLength;
		byte[] mainPuz = new byte[totalLength];
		System.arraycopy(toByteArray(FILE_MAGIC_STRING), 0, mainPuz, 2, 12);
		mainPuz[0x0e] = (byte)c_cib;
		mainPuz[0x0f] = (byte)(c_cib / 256);
		mainPuz[0x10] = (byte)(0x49 ^ (c_cib & 0xff));
		mainPuz[0x11] = (byte)(0x43 ^ (c_sol & 0xff));
		mainPuz[0x12] = (byte)(0x48 ^ (c_grid & 0xff));
		mainPuz[0x13] = (byte)(0x45 ^ (c_part ^ 0xff));
		mainPuz[0x14] = (byte)(0x41 ^ (c_cib / 256));
		mainPuz[0x15] = (byte)(0x54 ^ (c_sol / 256));
		mainPuz[0x16] = (byte)(0x45 ^ (c_grid / 256));
		mainPuz[0x17] = (byte)(0x44 ^ (c_part / 256));
		System.arraycopy(toByteArray(VERSION_STRING), 0, mainPuz, 0x18, 4);
		System.arraycopy(cib,0, mainPuz, 0x2c, 8);
		System.arraycopy(toByteArray(solution), 0, mainPuz, 0x34, nCells);
		System.arraycopy(toByteArray(playerState), 0, mainPuz, 0x34 + nCells, nCells);
		
		int index = 0x34 + nCells + nCells;
		for (String string : strings) {
			byte[] stringBytes = toByteArray(string + "\0");
			System.arraycopy(stringBytes, 0, mainPuz, index, stringBytes.length);
			index += stringBytes.length;
		}

		short fullChecksum = cksum_region(mainPuz, 2, totalLength - 2, (short)0);
		mainPuz[0] = (byte)fullChecksum;
		mainPuz[1] = (byte)(fullChecksum / 256);
		return mainPuz;
	}
	private short cksum_region(byte[] region, int index, int len, short cksum) {
		// use int to accumulate actual checksum so we don't have to worry about sign issues
		int intSum = cksum & 0xffff;
		for (int i = 0; i < len; ++i) {
			boolean lowBit = intSum % 1 == 1;
			intSum >>= 1;
			if (lowBit)
				intSum += 0x8000;
			intSum += region[index + i] & 0xff;
		}
		return (short)intSum;
	}
	
	// return solution and player state for grid, if possible, else null
	private Pair<String, String> getSolutionAndPlayerState(Grid grid) {
		int width = grid.getWidth();
		int height = grid.getHeight();
		int nCells = width * height;
		
		StringBuilder solutionBuilder = new StringBuilder(nCells);
		StringBuilder playerStringBuilder = new StringBuilder(nCells);
		for (int row = 0; row < height; ++row) {
			for (int column = 0; column < width; ++column) {
				Cell cell = grid.getCell(row, column);
				if (cell.isBlack()) {
					solutionBuilder.append('.');
					playerStringBuilder.append('.');
				} else {
					// TODO what to do if cell not black, but doesn't have contents either?
					String cellContents = cell.getContents();
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
			return string.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException unexpectedly caught, trying to getBytes for '" + string + "'.", e);
		}
	}
	
	// comparator to sort Clues in order, first by number, then by Across before Down (for identical numbers)
	private static final class ClueComparator implements Comparator<Clue> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Clue clue1, Clue clue2) {
			int clueNumber1 = clue1.getWordNumber();
			int clueNumber2 = clue2.getWordNumber();
			if (clueNumber1 < clueNumber2)
				return -1;
			if (clueNumber1 > clueNumber2)
				return 1;
			AcrossDownDirection direction1 = clue1.getAcrossDown();
			AcrossDownDirection direction2 = clue2.getAcrossDown();
			
			// we should never have to return 0, as we should never be comparing to the same clue
			// so we will treat that as an error condition
			if (direction1 == direction2)
				throw new RuntimeException("Unexpectedly found duplicate clue in ClueComparator: " + clue1 + ", " + clue2);
			return direction1 == AcrossDownDirection.ACROSS ? -1 : 1;
		}
		
	}
}
