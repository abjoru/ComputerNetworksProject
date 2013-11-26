package edu.fit.cs.computernetworks.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class IP {
	private final String wrappedIP;
	
	public IP(final String ip) {
		this.wrappedIP = ip;
	}
	
	public IP(final int ip) {
		this(toByteArray(ip));
	}
	
	public IP(final byte[] ip) {
		this(toString(ip));
	}
	
	private static String toString(final byte[] ip) {
		try {
			final InetAddress address = InetAddress.getByAddress(ip);
			return address.getHostAddress();
		} catch (final UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static byte[] toByteArray(final int ip) {
		final ByteBuffer buff = ByteBuffer.allocate(4);
		
		buff.put((byte) ((ip >> 24)));
		buff.put((byte) ((ip << 8) >> 24));
		buff.put((byte) ((ip << 16) >> 24));
		buff.put((byte) ((ip << 24) >> 24));
		
		return buff.array();
	}
	
	public int toInt() {
		final byte[] bytes = toByteArray();
		return ByteBuffer.wrap(bytes).getInt();
	}
	
	public byte[] toByteArray() {
		try {
			return InetAddress.getByName(wrappedIP).getAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String toString() {
		return wrappedIP;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((wrappedIP == null) ? 0 : wrappedIP.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IP other = (IP) obj;
		if (wrappedIP == null) {
			if (other.wrappedIP != null)
				return false;
		} else if (!wrappedIP.equals(other.wrappedIP))
			return false;
		return true;
	}
	
}