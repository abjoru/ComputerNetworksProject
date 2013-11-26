package edu.fit.cs.computernetworks.utils;


/**
 * General purpose network address utils.
 * 
 * @author Andreas Bjoru
 *
 */
public class NetUtils {
	
	private NetUtils() {
		// Utility class - no instantiation allowed
	}
	
	public static IP wrap(final String ip) {
		return new IP(ip);
	}
	
	public static IP wrap(final int ip) {
		return new IP(ip);
	}
	
	public static IP wrap(final byte[] ip) {
		return new IP(ip);
	}

	public static IP networkAddress(final IP ip, final IP mask) {
		final byte[] networkAddress = new byte[ip.toByteArray().length];
		for (int i = 0; i < ip.toByteArray().length; i++) {
			networkAddress[i] = (byte) (ip.toByteArray()[i] & mask.toByteArray()[i]);
		}
		
		return NetUtils.wrap(networkAddress);
	}
	
	public static byte[] macToByteArray(final String mac) {
		final String[] segments = mac.split(":");
		final byte[] res = new byte[segments.length];
		for (int i = 0; i < segments.length; i++) {
			res[i] = (byte) (Integer.parseInt(segments[i], 16) & 0xff);
		}
		
		return res;
	}
	
}
