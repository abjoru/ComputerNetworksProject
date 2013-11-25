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

}
