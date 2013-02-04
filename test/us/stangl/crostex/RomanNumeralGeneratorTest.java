/**
 * Copyright 2008, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * JUnit tests for RomanNumeralGenerator.
 */
public class RomanNumeralGeneratorTest  {
	
	/**
	 * Test all Roman numeral values from 1 to 23000, going from int -> Roman numeral -> back to int
	 * confirming that same value is preserved
	 */
	@Test
	public void testHappyPath() {
		RomanNumeralGenerator generator = new RomanNumeralGenerator();
		
		for (int i = 1; i < 4000; ++i) {
			assertEquals(i, generator.getValueOfRomanNumeral(generator.getRomanNumeral(i)));
		}
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkConvertZero() {
		RomanNumeralGenerator.getRomanNumeral(0);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkConvertNegative() {
		RomanNumeralGenerator.getRomanNumeral(-1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void checkMIM() {
		RomanNumeralGenerator.getValueOfRomanNumeral("MIM");
	}
}
