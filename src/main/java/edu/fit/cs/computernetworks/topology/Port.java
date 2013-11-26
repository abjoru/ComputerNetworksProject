package edu.fit.cs.computernetworks.topology;

import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

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
	
	public IP toNetworkAddress() {
		final IP ip = NetUtils.wrap(this.ip);
		final IP mask = NetUtils.wrap(this.mask);
		return NetUtils.networkAddress(ip, mask);
	}

	public boolean matches(final IP networkAddr) {
		return networkAddr.equals(toNetworkAddress());
	}
	
	public boolean machesDestinationIP(final IP destIP) {
		final IP mask = NetUtils.wrap(this.mask);
		final IP netAddr = NetUtils.networkAddress(destIP, mask);
		return matches(netAddr);
	}
}
