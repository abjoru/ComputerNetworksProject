package edu.fit.cs.computernetworks;

import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.Topology;

public class NetworkRouter extends AbstractNetworkNode<Router> {
	
	private static final int DEFAULT_MTU = 1400;

	public NetworkRouter(final Topology topology, final Router descriptor) {
		super(topology, descriptor);
	}
	

	@Override
	public int mtu(final String localIp) {
		for (final Port port : descriptor.ports) {
			if (port.ip.equals(localIp)) {
				return port.mtu;
			}
		}
		
		return DEFAULT_MTU;
	}

}
