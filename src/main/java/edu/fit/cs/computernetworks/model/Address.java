package edu.fit.cs.computernetworks.model;

import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class Address {
	public static final short DEFAULT_PORT = 1111;
	
	private final String sourceIp;
	private final String destIp;
	
	private final short sourcePort;
	private final short destPort;
	
	
	public Address(final String source, final String dest) {
		this(source, dest, DEFAULT_PORT, DEFAULT_PORT);
	}

	public Address(final String sourceIP, final String destIP, final short sourcePort, final short destPort) {
		this.sourceIp = sourceIP;
		this.destIp = destIP;
		this.sourcePort = sourcePort;
		this.destPort = destPort;
	}
	
	public IP getSourceAddress() {
		return NetUtils.wrap(sourceIp);
	}

	public IP getDestinationAddress() {
		return NetUtils.wrap(destIp);
	}

	public short getSourcePort() {
		return sourcePort;
	}
	
	public short getDestPort() {
		return destPort;
	}
	
}
