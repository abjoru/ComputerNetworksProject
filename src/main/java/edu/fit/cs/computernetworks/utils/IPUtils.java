package edu.fit.cs.computernetworks.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtils {

	public static byte[] ipToByteArray(final String ip) {
		try {
			return InetAddress.getByName(ip).getAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] networkAddress(final byte[] ip, final byte[] mask) {
		final byte[] networkAddress = new byte[ip.length];
		for (int i = 0; i < ip.length; i++) {
			networkAddress[i] = (byte) (ip[i] & mask[i]);
		}
		
		return networkAddress;
	}
	
}
