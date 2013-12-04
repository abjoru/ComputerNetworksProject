package edu.fit.cs.computernetworks.model;

import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class Address {
	private final String sourceIp;
	private final String destIp;
	
	private byte[] sourceMac;
	private byte[] destMac;
	
	public Address(final String source, final String dest) {
		this.sourceIp = source;
		this.destIp = dest;
	}
	
	public IP getSourceAddress() {
		return NetUtils.wrap(sourceIp);
	}

	public IP getDestinationAddress() {
		return NetUtils.wrap(destIp);
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
