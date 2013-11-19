package edu.fit.cs.computernetworks.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

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
	
	public static int byteArrayIpToInt(final byte[] ip) {
		return ByteBuffer.wrap(ip).getInt();
	}
	
	public static byte[] intIpToByteArray(final int ip) {
		final ByteBuffer buff = ByteBuffer.allocate(4);
		
		buff.put((byte) ((ip >> 24)));
		buff.put((byte) ((ip << 8) >> 24));
		buff.put((byte) ((ip << 16) >> 24));
		buff.put((byte) ((ip << 24) >> 24));
		
		return buff.array();
	}
	
}
