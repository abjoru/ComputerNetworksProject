package edu.fit.cs.computernetworks.model;

import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class Address {
	public static final short DEFAULT_PORT = 1111;
	
	private final IP sourceIp;
	private final IP destIp;
	
	private byte[] sourceMac;
	private byte[] destMac;
	
	private short sourcePort;
	private short destPort;
	
	
	public Address(final String source, final String dest) {
		this(source, dest, DEFAULT_PORT, DEFAULT_PORT);
	}

	public Address(final String sourceIP, final String destIP, final short sourcePort, final short destPort) {
		this.sourceIp = NetUtils.wrap(sourceIP);
		this.destIp = NetUtils.wrap(destIP);
		this.sourcePort = sourcePort;
		this.destPort = destPort;
	}
	
	public IP getSourceAddress() {
		return sourceIp;
	}
	
	public IP getDestinationAddress() {
		return destIp;
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

	public short getSourcePort() {
		return sourcePort;
	}
	
	public void setSourcePort(final short sourcePort) {
		this.sourcePort = sourcePort;
	}
	
	public short getDestPort() {
		return destPort;
	}
	
	public void setDestPort(final short destPort) {
		this.destPort = destPort;
	}
	
}
