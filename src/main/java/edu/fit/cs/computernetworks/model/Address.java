package edu.fit.cs.computernetworks.model;

import edu.fit.cs.computernetworks.utils.NetUtils;

public class Address {
	private final String sourceIp;
	private final String destIp;
	
	public Address(final String source, final String dest) {
		this.sourceIp = source;
		this.destIp = dest;
	}

	public int sourceAddressToInt() {
		final byte[] bip = NetUtils.ipToByteArray(sourceIp);
		return NetUtils.byteArrayIpToInt(bip);
	}

	public int destAddressToInt() {
		final byte[] bip = NetUtils.ipToByteArray(destIp);
		return NetUtils.byteArrayIpToInt(bip);
	}
	
	public String getSourceAddress() {
		return sourceIp;
	}

	public String getDestinationAddress() {
		return destIp;
	}

}
