package edu.fit.cs.computernetworks.model;

import edu.fit.cs.computernetworks.utils.IPUtils;

public class Address {
	private final String sourceIp;
	private final String destIp;
	
	public Address(final String source, final String dest) {
		this.sourceIp = source;
		this.destIp = dest;
	}

	public int sourceAddressToInt() {
		final byte[] bip = IPUtils.ipToByteArray(sourceIp);
		return IPUtils.byteArrayIpToInt(bip);
	}

	public int destAddressToInt() {
		final byte[] bip = IPUtils.ipToByteArray(destIp);
		return IPUtils.byteArrayIpToInt(bip);
	}
	
	public String getSourceAddress() {
		return sourceIp;
	}

	public String getDestinationAddress() {
		return destIp;
	}

}
