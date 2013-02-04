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
	
	/** for each element [i], where i > 0, holds list of RomanNumeral of the specified length */
	List<List<String>> bucketsOfLength_ = new ArrayList<List<String>>();
	
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
		
		int temp = value;
		
		// First generate M's until left with value less than 1000
		while (temp >= 1000) {
			retval.append('M');
			temp -= 1000;
		}
		
		if (temp >= 900) {
			retval.append("CM");
			temp -= 900;
		}
		
		if (temp >= 500) {
			retval.append("D");
			temp -= 500;
		}
		
		if (temp >= 400) {
			retval.append("CD");
			temp -= 400;
		}
		
		while (temp >= 100) {
			retval.append('C');
			temp -= 100;
		}
		
		if (temp >= 90) {
			retval.append("XC");
			temp -= 90;
		}
		
		if (temp >= 50) {
			retval.append('L');
			temp -= 50;
		}
		
		if (temp >= 40) {
			retval.append("XL");
			temp -= 40;
		}
		
		while (temp >= 10) {
			retval.append('X');
			temp -= 10;
		}
		
		if (temp >= 9) {
			retval.append("IX");
			temp -= 9;
		}
		
		if (temp >= 5) {
			retval.append('V');
			temp -= 5;
		}
		
		if (temp >= 4) {
			retval.append("IV");
			temp -= 4;
		}
		
		while (temp >= 1) {
			retval.append('I');
			--temp;
		}
		
		return retval.toString();
	}

	public static int getValueOfRomanNumeral(String numeral) {
		int retval = 0;
		int index = 0;
		while (numeral.startsWith("M", index)) {
			retval += 1000;
			++index;
		}
		
		if (numeral.startsWith("CM", index)) {
			retval += 900;
			index += 2;
		}
		
		if (numeral.startsWith("D", index)) {
			retval += 500;
			++index;
		}
		
		if (numeral.startsWith("CD", index)) {
			retval += 400;
			index += 2;
		}
		
		while (numeral.startsWith("C", index)) {
			retval += 100;
			++index;
		}
		
		if (numeral.startsWith("XC", index)) {
			retval += 90;
			index += 2;
		}
		
		if (numeral.startsWith("L", index)) {
			retval += 50;
			++index;
		}
		
		if (numeral.startsWith("XL", index)) {
			retval += 40;
			index += 2;
		}
		
		while (numeral.startsWith("X", index)) {
			retval += 10;
			++index;
		}
		
		if (numeral.startsWith("IX", index)) {
			retval += 9;
			index += 2;
		}
		
		if (numeral.startsWith("V", index)) {
			retval += 5;
			++index;
		}
		
		if (numeral.startsWith("IV", index)) {
			retval += 4;
			index += 2;
		}
		
		while (numeral.startsWith("I", index)) {
			retval++;
			++index;
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
		for (String numeral : list) {
			System.out.println(numeral);
		}
//		for (int i = 1; i <= 1000; ++i) {
//			System.out.println(i + " = " + instance.getRomanNumeral(i));
//		}
		
	}
	
	private List<String> getBucketOfLength(int length) {
		while (bucketsOfLength_.size() <= length) {
			bucketsOfLength_.add(new ArrayList<String>());
		}
		return bucketsOfLength_.get(length);
	}
}
