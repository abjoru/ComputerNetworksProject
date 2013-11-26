package edu.fit.cs.computernetworks.utils;

public class ErrorCheckUtils {
	public static final int CRC_POLYNOMIAL = 0xEDB88320;
	
	public static int crc32(final byte[] data) {
		int crc = 0xFFFFFFFF;
		
		for (final byte b : data) {
			int temp = (crc ^ b) & 0xff;
			
			// read 8 bits one at the time
			for (int i = 0; i < 8; i++) {
				if ((temp & 1) == 1)
					temp = (temp >>> 1) ^ CRC_POLYNOMIAL;
				else
					temp = (temp >>> 1);
			}
			
			crc = (crc >>> 8) ^ temp;
		}
		
		return crc ^ 0xffffffff;
	}
	
	/**
	 * Calculate checksum of the given byte array.
	 */
	public static int checksum(final byte[] data) {
		// Based on http://stackoverflow.com/questions/4113890/how-to-calculate-the-internet-checksum-from-a-byte-in-java
		int length = data.length;
		int sum = 0;
		int i = 0;

		while (length > 1) {
			sum += (((data[i] << 8) & 0xff00) | ((data[i + 1]) & 0xff));
			
			if ((sum & 0xffff0000) > 0) {
				sum &= 0xffff;
				sum += 1;
			}
			
			i += 2;
			length -= 2;
		}
		
		if (length > 0) {
			sum += (data[i] << 8 & 0xff00);
			if ((sum & 0xffff0000) > 0) {
				sum = sum & 0xffff;
				sum += 1;
			}
		}
		
		return ~sum & 0xffff;
	}

}
