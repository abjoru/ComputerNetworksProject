package edu.fit.cs.computernetworks;

import java.util.Collection;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.DataLinkLayer;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.Tuple;

public abstract class NetworkNode<T extends Node> {
	
	protected enum Transmit {
		RECEIVE, SEND
	}
	
	protected final T descriptor;
	protected final NodeManager manager;
	protected final DataLinkLayer linkLayer;
	
	public NetworkNode(final Topology topo, final T descriptor, final NodeManager manager) {
		this.descriptor = descriptor;
		this.manager = manager;
		this.linkLayer = new DataLinkLayer(topo, descriptor);
	}
	
	public abstract void networkLayer(final byte[] msg, final Transmit transmit, final Address addr);
	
	public void linkLayer(final byte[] message, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND:
			// Fragment message and deliver to physical layer
			final Tuple<String, Collection<IPPacket>> t = linkLayer.handleSend(message, addr);
			for (final IPPacket pkg : t._2) {
				physicalLayer(pkg, transmit, t._1);
			}
			
			break;
		case RECEIVE:
			final IPPacket pkg = IPPacket.fromByteArray(message);
			final byte[] data = pkg.getData();
			
			networkLayer(data, transmit, null);
			
			break;
		default: // no-op
		}
	}
	
	public void physicalLayer(final IPPacket packet, final Transmit transmit, final String macAddr) {
		switch (transmit) {
		case SEND:
			final NetworkNode<? extends Node> node = manager.route(macAddr);
			node.physicalLayer(packet, Transmit.RECEIVE, null);
			break;
		case RECEIVE:
			linkLayer(packet.toByteArray(), transmit, null);
			break;
		default: // no-op
		}
	}
	
	
}
