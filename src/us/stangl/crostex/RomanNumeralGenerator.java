/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator for Roman Numerals.
 * Not thread-safe. If shared between threads, need external synchronization.
 */
public class RomanNumeralGenerator {
	/** largest numeral we can generate */
	public static final int MAX_VALUE = 3999;
	
	// arrays of roman numeral parts and their corresponding numeric value 
	private static final String[] ROMAN_NUMERAL_PRIMITIVE_STRINGS =
		{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
	private static final int[] ROMAN_NUMBER_PRIMITIVE_VALUES = 
		{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
	
	/** for each element [i], where i > 0, holds list of RomanNumeral of the specified length */
	List<List<String>> bucketsOfLength = new ArrayList<List<String>>();
	
	/**
	 * Return Roman numeral string representation of specified value.
	 * Cannot generate Roman numerals for values less than 1, since Romans only
	 * had numerals for counting numbers, and cannot generate for values greater than 3999
	 * since proper Roman numerals for these values require the bar-over-the-number notation.
	 * @param value value to convert to Roman numeral
	 * @return Roman numeral string representation of specified value
	 */
	public static String getRomanNumeral(int value) {
		if (value < 1 || value > MAX_VALUE)
			throw new IllegalArgumentException("Cannot generate Roman numeral for " + value + ", only value from 1 thru 3999");
		
		//TODO if performance suffers, consider making this StringBuilder an instance variable
		StringBuilder retval = new StringBuilder();
		
		for (int i = 0; i < ROMAN_NUMBER_PRIMITIVE_VALUES.length; ++i) {
			int val = ROMAN_NUMBER_PRIMITIVE_VALUES[i];
			String string = ROMAN_NUMERAL_PRIMITIVE_STRINGS[i];
			while (value >= val) {
				retval.append(string);
				value -= val;
			}
		}
		
		return retval.toString();
	}

	public static int getValueOfRomanNumeral(String numeral) {
		int retval = 0;
		int index = 0;
		for (int i = 0; i < ROMAN_NUMBER_PRIMITIVE_VALUES.length; ++i) {
			int val = ROMAN_NUMBER_PRIMITIVE_VALUES[i];
			String string = ROMAN_NUMERAL_PRIMITIVE_STRINGS[i];
			while (numeral.startsWith(string, index)) {
				retval += val;
				index += string.length();
			}
		}
		
		if (index != numeral.length())
			throw new IllegalArgumentException("Could not parse Roman numeral " + numeral + " at index " + index);

		return retval;
	}
	
	public List<String> generateAllNumeralsOfLength(int length) {
		if (length < 1)
			throw new IllegalArgumentException("Illegal argument " + length + " to generateAllNumeralsOfLength");
		List<String> bucket = getBucketOfLength(length);
		
		if (bucket.isEmpty()) {
			// Compute upper limit for number
			int limit = length * 1000;
			if (limit > MAX_VALUE)
				limit = MAX_VALUE;
			
			for (int i = 1; i <= limit; ++i) {
				String numeral = getRomanNumeral(i);
				if (numeral.length() == length)
					bucket.add(numeral);
			}
		}
		
		return bucket;
	}
	
	public static void main(String[] args) {
		RomanNumeralGenerator instance = new RomanNumeralGenerator();
		
		List<String> list = instance.generateAllNumeralsOfLength(7);
		for (String numeral : list)
			System.out.println(numeral);
//		for (int i = 1; i <= 1000; ++i)
//			System.out.println(i + " = " + instance.getRomanNumeral(i));
		
	}
	
	private List<String> getBucketOfLength(int length) {
		while (bucketsOfLength.size() <= length)
			bucketsOfLength.add(new ArrayList<String>());
		return bucketsOfLength.get(length);
	}
}
