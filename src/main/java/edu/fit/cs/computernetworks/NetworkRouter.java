package edu.fit.cs.computernetworks;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.Topology;

public class NetworkRouter extends NetworkNode<Router> {
	
	public NetworkRouter(final Topology topology, final Router descriptor, final NodeManager mgr) {
		super(topology, descriptor, mgr);
	}
	
	@Override
	public void networkLayer(final byte[] message, final Transmit transmit, final Address addr) {
		
	}

}
