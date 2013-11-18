package edu.fit.cs.computernetworks;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.OSILayerPacket;
import edu.fit.cs.computernetworks.model.TransportLayer;
import edu.fit.cs.computernetworks.model.TransportLayerPacket;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.utils.SimpleLogger;

public class NetworkNode implements Runnable {
	
	private enum Transmit {
		RECEIVE, SEND
	}
	
	private final Node self;
	private final TransportLayer transportLayer = null;
	private final SimpleLogger logger = new SimpleLogger("Node address, etc");
	
	public NetworkNode(final Node topologyNode) {
		this.self = topologyNode;
	}
	
	private void application(byte[] assemble) {
		logger.log("Received packet: " + assemble);
	}
	
	public void transport(final byte[] msg, final Transmit type, final Address addr) {
		switch (type) {
		case SEND: // I.e. this node sends data to some destination..
			for (final OSILayerPacket pkg : TransportLayerPacket.from(msg, addr, self.getPorts())) {
				routing(pkg.toByteArray(), Transmit.SEND);
			}
			break;
		case RECEIVE: // I.e. received transmission from lower levels..
			// Effectively transform input to TransportLayerPacket
			
			TransportLayerPacket segment = transportLayer.handleReceive(msg);
			if (segment.matchesAddress(self)) {
				final long sid = segment.getSegmentId();
				if (sid > 0) { // does no segments represent -1?
					if (transportLayer.queue(segment)) { // return true if all segments received?
						application(transportLayer.assemble(segment));
					}
				}
			}
			break;
		default: // no-op
		}
	}

	public void routing(final byte[] pkg, final Transmit type) {
		
	}
	
	public void dataLink() {
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
