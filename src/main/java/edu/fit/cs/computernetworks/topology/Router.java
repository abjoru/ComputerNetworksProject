package edu.fit.cs.computernetworks.topology;

import java.util.Arrays;
import java.util.List;

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

}
