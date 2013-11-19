package edu.fit.cs.computernetworks.topology;

import java.util.Arrays;

import edu.fit.cs.computernetworks.utils.IPUtils;

public class Port {

	public String ip;
	public String mask;
	public String mac;
	public int mtu;
	
	public Port() {
		// Default constructor required by JSON mapper
	}
	
	public Port(final String ip, final String mask, final String mac, final int mtu) {
		this.ip = ip;
		this.mask = mask;
		this.mac = mac;
		this.mtu = mtu;
	}
	
	public byte[] toNetworkAddress() {
		byte[] ipBytes = IPUtils.ipToByteArray(ip);
		byte[] maskBytes = IPUtils.ipToByteArray(mask);
		return IPUtils.networkAddress(ipBytes, maskBytes);
	}

	public boolean matches(byte[] networkAddr) {
		return Arrays.equals(networkAddr, toNetworkAddress());
	}
}
