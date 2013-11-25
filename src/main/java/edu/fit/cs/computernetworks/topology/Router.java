package edu.fit.cs.computernetworks.topology;

import java.util.Arrays;
import java.util.List;

import edu.fit.cs.computernetworks.utils.NetUtils;

public class Router extends Node {
	public List<Port> ports;
	
	public Port getPortForNetwork(final byte[] networkAddr) {
		for (final Port p : ports) {
			if (Arrays.equals(networkAddr, p.toNetworkAddress())) {
				return p;
			}
		}
		
		return null;
	}
	
	public Port getPortForDestinationIP(final int destIP) {
		for (final Port p : ports) {
			if (p.machesDestinationIP(destIP)) {
				return p;
			}
		}
		
		return null;
	}
	
	@Override
	public byte[] nextHopTo(byte[] destIP) {
		for (final Port p : ports) {
			final byte[] netAddr = NetUtils.networkAddress(destIP, NetUtils.ipToByteArray(p.mask));
			for (final RoutingEntry re : routing) {
				if (Arrays.equals(netAddr, re.networkAddress())) {
					return NetUtils.ipToByteArray(re.nextHop);
				}
			}
		}
		
		return null;
	}

}
