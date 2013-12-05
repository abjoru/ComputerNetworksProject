package edu.fit.cs.computernetworks.model;

import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class Address {
	public static final int DEFAULT_PORT = 1111;
	
	private final IP sourceIp;
	private final IP destIp;
	
	private final int sourcePort;
	private final int destPort;
	
	private byte[] sourceMac;
	private byte[] destMac;
	
	public Address(final String source, final String dest) {
		this(source, DEFAULT_PORT, dest, DEFAULT_PORT);
	}
	
	public Address(final String sourceIp, final int sourcePort, final String destIp, final int destPort) {
		this.sourceIp = NetUtils.wrap(sourceIp);
		this.destIp = NetUtils.wrap(destIp);
		this.sourcePort = sourcePort;
		this.destPort = destPort;
	}
	
	public IP getSourceAddress() {
		return sourceIp;
	}
	
	public int getSourcePort() {
		return sourcePort;
	}

	public IP getDestinationAddress() {
		return destIp;
	}
	
	public int getDestinationPort() {
		return destPort;
	}
	
	public byte[] getSourceMac() {
		return sourceMac;
	}
	
	public void setSourceMac(final byte[] sourceMac) {
		this.sourceMac = sourceMac;
	}
	
	public byte[] getDestMac() {
		return destMac;
	}
	
	public void setDestMac(final byte[] destMac) {
		this.destMac = destMac;
	}

}
