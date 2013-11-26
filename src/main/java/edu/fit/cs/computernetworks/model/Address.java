package edu.fit.cs.computernetworks.model;

import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class Address {
	private final String sourceIp;
	private final String destIp;
	
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

}
