package edu.fit.cs.computernetworks;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Topology;

public abstract class AbstractNetworkNode<T extends Node> {
	
	protected enum Transmit {
		RECEIVE, SEND
	}
	
	private int ident = 0;
	
	protected final T descriptor;
	protected final Topology topology;
	
	public AbstractNetworkNode(final Topology topo, final T descriptor) {
		this.descriptor = descriptor;
		this.topology = topo;
	}
	
	public abstract void networkLayer(final byte[] msg, final Transmit transmit, final Address addr);
	
	public void linkLayer(final byte[] message, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND:
			// Fragment message and deliver to physical layer
			final int mtu = mtu(addr.getSourceAddress());
			final String destMac = topology.arpResolve(addr.getDestinationAddress());
			
			if (mtu < message.length) {
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
	
	public void physicalLayer(final byte[] packet, final Transmit transmit, final String macAddr) {
		switch (transmit) {
		case SEND:
			final AbstractNetworkNode<? extends Node> nextHop = topology.machineFor(macAddr);
			nextHop.physicalLayer(packet, Transmit.RECEIVE, null);
			break;
		case RECEIVE:
			linkLayer(packet, transmit, null);
			break;
		default: // no-op
		}
	}
	
	abstract int mtu(final String localIp);
	
}
