package edu.fit.cs.computernetworks;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.Frame;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.SimpleLogger;

public abstract class AbstractNetworkNode<T extends Node> {
	
	protected enum Transmit {
		RECEIVE, SEND
	}
	
	private int ident = 0;
	
	protected final T descriptor;
	protected final Topology topology;
	
	private SimpleLogger logger = new SimpleLogger("Some logger");
	
	public AbstractNetworkNode(final Topology topo, final T descriptor) {
		this.descriptor = descriptor;
		this.topology = topo;
	}
	
	public  void networkLayer(final byte[] msg, final Transmit transmit, final Address addr) {
		
	}
	
	public abstract int mtu(final String localIp);
	
	// TODO move ippkg up
	public void linkLayer(final byte[] message, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND:
			// Fragment message and deliver to physical layer
			final String destMac = topology.arpResolve(addr.getDestinationAddress());
			
			if (mtu(addr.getSourceAddress()) < message.length) {
				// TODO fragment message
			} else {
				final IPPacket pkg = new IPPacket(ident++, addr.sourceAddressToInt(), addr.destAddressToInt());
				pkg.setData(message);
				physicalLayer(pkg.toByteArray(), transmit, destMac);
			}
			
			break;
		case RECEIVE:
			final IPPacket pkg = IPPacket.fromByteArray(message);
			// TODO error correction/detection?
			final byte[] data = pkg.getData();
			
			networkLayer(data, transmit, null);
			
			break;
		default: // no-op
		}
	}
	
	// TODO move ethernet frame up
	public void physicalLayer(final byte[] packet, final Transmit transmit, final String macAddr) {
		switch (transmit) {
		case SEND:
			logger.log("Sending message on physical layer");
			final Frame frame = new Frame();
			frame.setPayload(packet);
			
			// Find actual machine from MAC address and deliver packet
			final AbstractNetworkNode<? extends Node> nextHop = topology.machineFor(macAddr);
			nextHop.physicalLayer(frame.toByteArray(), Transmit.RECEIVE, null);
			break;
		case RECEIVE:
			final Frame frameRecevied = Frame.from(packet);
			// TODO checksum
			
			// Just send it on to the link-layer
			linkLayer(frameRecevied.getPayload(), transmit, null);
			break;
		default: // no-op
		}
	}
	
}
