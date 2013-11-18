package edu.fit.cs.computernetworks.topology;

import edu.fit.cs.computernetworks.utils.IPUtils;

public class Port {

	private String ip;
	private String mask;
	private String mac;
	private int mtu;
	
	public String getIp() {
		return ip;
	}
	
	public String getMask() {
		return mask;
	}
	
	public String getMac() {
		return mac;
	}
	
	public int getMtu() {
		return mtu;
	}
	
	public byte[] toNetworkAddress() {
		byte[] ipBytes = IPUtils.ipToByteArray(ip);
		byte[] maskBytes = IPUtils.ipToByteArray(mask);
		return IPUtils.networkAddress(ipBytes, maskBytes);
	}
}
