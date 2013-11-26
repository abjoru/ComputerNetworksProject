package edu.fit.cs.computernetworks.topology;

import java.util.List;

import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class Router extends Node {
	public List<Port> ports;
	
	public Port getPortForNetwork(final IP networkAddr) {
		for (final Port p : ports) {
			if (networkAddr.equals(p.toNetworkAddress())) {
				return p;
			}
		}
		
		return null;
	}
	
	public Port getPortForDestinationIP(final IP destIP) {
		for (final Port p : ports) {
			if (p.machesDestinationIP(destIP)) {
				return p;
			}
		}
		
		return null;
	}
	
	@Override
	public IP nextHopTo(final IP destIP) {
		for (final Port p : ports) {
			final IP netAddr = NetUtils.networkAddress(destIP, NetUtils.wrap(p.mask));
			for (final RoutingEntry re : routing) {
				if (netAddr.equals(re.networkAddress())) {
					return NetUtils.wrap(re.nextHop);
				}
			}
		}
		
		return null;
	}

}
