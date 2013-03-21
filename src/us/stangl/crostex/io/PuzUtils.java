/**
 * Copyright 2013, Alex Stangl. See LICENSE for licensing details.
 */
package us.stangl.crostex.io;

/**
 * Misc. static utility methods to use as part of PUZ serialization.
 * @author Alex Stangl
 */
public class PuzUtils {
	// return checksum of len bytes starting at region[index], and initial checksum value cksum
	public static short cksum_region(byte[] region, int index, int len, short cksum) {
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
