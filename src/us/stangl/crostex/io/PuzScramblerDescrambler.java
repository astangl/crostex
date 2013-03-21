/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

import us.stangl.crostex.util.StringUtils;

/**
 * Helper to scramble or descramble a PUZ-format grid.
 * @author Alex Stangl
 */
public final class PuzScramblerDescrambler {
	/** lowest valid key value */
	public static final int MIN_KEY_VALUE = 1000;
	
	/** highest valid key value */
	public static final int MAX_KEY_VALUE = 9999;
	
	/**
	 * Scramble specifying grid, producing byte[] representing scrambled puzzle solution, as stored in the PUZ stream
	 * @param grid grid to scramble
	 * @param key key to use to scramble grid, should be in range MIN_KEY_VALUE..MAX_KEY_VALUE
	 */
	public byte[] scramble(IoGrid grid, int key) {
		if (key < MIN_KEY_VALUE || key > MAX_KEY_VALUE)
			throw new IllegalArgumentException("Key" + key + " outside expected range " + MIN_KEY_VALUE + ".." + MAX_KEY_VALUE);
		String colFirstString = toColumnThenRowString(grid);
		int strlen = colFirstString.length();

		// convert key into array of individual decimal digits for convenience
		int[] keyArray = new int[4];
		for (int i = 3; i >= 0; --i) {
			keyArray[i] = key % 10;
			key /= 10;
		}

		String scrambled = colFirstString;
		for (int key_num : keyArray) {
			StringBuilder builder = new StringBuilder(strlen);
			for (int i = 0; i < strlen; ++i) {
				char c = (char)(scrambled.charAt(i) + keyArray[i % 4]);
				// if we go outside range of uppercase ASCII characters, wrap back to A
				if (c > 'Z')
					c -= 26;
				builder.append(c);
			}
			scrambled = scrambleString(StringUtils.rotateLeft(builder.toString(), key_num));
		}
		return toByteArray(scrambled, grid);
	}
	
	/**
	 * Automatically descramble scrambled grid by brute-force trying all 9000 possible keys,
	 * and comparing solution's checksum to known good solution checksum.
	 * NOTE: assumes (maybe not correctly -- revisit later) that only the good solution grid
	 *       will match this checksum.
	 * @param grid grid to descramble
	 * @param descrambledSolutionChksum checksum for good solution
	 * @return byte[] representing descrambled puzzle solution, as stored in the PUZ stream
	 */
	public byte[] autoDescramble(IoGrid grid, short descrambledSolutionChksum) {
	
		for (int key = 1000; key < 10000; ++key) {
			String possibleSolution = descramble(grid, key);
			byte[] bytes = possibleSolution.getBytes();
			short chksum = PuzUtils.cksum_region(bytes, 0, bytes.length, (short)0);
			if (chksum == descrambledSolutionChksum)
				return toByteArray(possibleSolution, grid);
		}
		throw new RuntimeException("No solution found matching " + descrambledSolutionChksum);
	}

	/**
	 * Descramble specifying grid, producing byte[] representing descrambled puzzle solution, as stored in the PUZ stream
	 * @param grid grid to descramble
	 * @param key key to use to descramble grid, should be in range MIN_KEY_VALUE..MAX_KEY_VALUE
	 * @return descrambled puzzle solution string, in column major order, with black cells omitted
	 */
	public String descramble(IoGrid grid, int key) {
		if (key < MIN_KEY_VALUE || key > MAX_KEY_VALUE)
			throw new IllegalArgumentException("Key" + key + " outside expected range " + MIN_KEY_VALUE + ".." + MAX_KEY_VALUE);
		String colFirstString = toColumnThenRowString(grid);
		int strlen = colFirstString.length();

		// convert key into array of individual decimal digits for convenience
		int[] keyArray = new int[4];
		for (int i = 3; i >= 0; --i) {
			keyArray[i] = key % 10;
			key /= 10;
		}
		String workString = colFirstString;
		for (int j = 3; j >= 0; --j) {
			int key_num = keyArray[j];
			
			workString = StringUtils.rotateRight(deScrambleString(workString), key_num);
			StringBuilder builder = new StringBuilder(strlen);
			for (int i = 0; i < strlen; ++i) {
				char c = (char)(workString.charAt(i) - keyArray[i % 4]);
				// if we go outside range of uppercase ASCII characters, wrap back to Z
				if (c < 'A')
					c += 26;
				builder.append(c);
			}
			workString = builder.toString();
		}
		return workString;
		//return toByteArray(workString, grid);
	}

	// produce grid string from IoGrid, by traversing by column first, then by row,
	// as opposed to the usual direction, and omiting black squares
	private String toColumnThenRowString(IoGrid grid) {
		StringBuilder retval = new StringBuilder();
		int width = grid.getWidth();
		int height = grid.getHeight();
		for (int column = 0; column < width; ++column) {
			for (int row = 0; row < height; ++row) {
				if (! grid.isBlackCell(row, column)) {
					// intentionally letting charAt blow up on next line if cell is empty, since this shouldn't happen
					retval.append(grid.getCellContents(row, column).charAt(0));
				}
			}
		}
		return retval.toString();
	}
	
	private byte[] toByteArray(String scrambled, IoGrid grid) {
		int width = grid.getWidth();
		int height = grid.getHeight();
		int nCells = width * height;
		byte[] retval = new byte[nCells];
		byte[] scrambledBytes = scrambled.getBytes();
		if (scrambledBytes.length != scrambled.length())
			throw new RuntimeException("String conversion problem: scrambled string of length "
					+ scrambled.length() + " produced byte array of length " + scrambledBytes.length);
		int index = 0;
		for (int column = 0; column < width; ++column)
			for (int row = 0; row < height; ++row)
				retval[row * width + column] = grid.isBlackCell(row, column) ? (byte)'.' : scrambledBytes[index++];
		return retval;
	}

	// scramble string, used internally as part of scrambling logic
	String scrambleString(String string) {
		int strlen = string.length();
		int mid = strlen / 2;
		StringBuilder retval = new StringBuilder(strlen);
		for (int i = 0; i < mid; ++i) {
			retval.append(string.charAt(mid + i))
				.append(string.charAt(i));
		}
		if (strlen % 2 != 0)
			retval.append(string.charAt(strlen - 1));
		return retval.toString();
	}
	
	// reverse scrambleString operation, used internally as part of descrambling logic
	String deScrambleString(String string) {
		int strlen = string.length();
		StringBuilder retval = new StringBuilder(strlen);
		for (int i = 1; i < strlen; i += 2)
			retval.append(string.charAt(i));
		for (int i = 0; i < strlen; i += 2)
			retval.append(string.charAt(i));
		return retval.toString();
	}


}
