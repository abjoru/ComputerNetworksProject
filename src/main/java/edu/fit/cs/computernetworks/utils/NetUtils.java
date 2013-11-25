package edu.fit.cs.computernetworks.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class NetUtils {

	public static byte[] ipToByteArray(final String ip) {
		try {
			return InetAddress.getByName(ip).getAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static int ipToInt(final String ip) {
		final byte[] bytes = ipToByteArray(ip);
		return byteArrayIpToInt(bytes);
	}
	
	public static String intIPToString(final int ip) {
		try {
			final InetAddress address = InetAddress.getByAddress(intIpToByteArray(ip));
			return address.getHostAddress();
		} catch (final UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[] networkAddress(final String ip, final String mask) {
		final byte[] bIp = ipToByteArray(ip);
		final byte[] bMask = ipToByteArray(mask);
		return networkAddress(bIp, bMask);
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
	
	public static byte[] macToByteArray(final String mac) {
		final String[] segments = mac.split(":");
		final byte[] res = new byte[segments.length];
		for (int i = 0; i < segments.length; i++) {
			// res[i] = Byte.parseByte(segments[i], 16);
			res[i] = (byte) (Integer.parseInt(segments[i], 16) & 0xff);
		}
		
		return res;
	}
	
}
