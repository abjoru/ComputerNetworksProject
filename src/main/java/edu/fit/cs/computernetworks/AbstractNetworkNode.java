package edu.fit.cs.computernetworks;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.EthernetFrame;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.NetUtils;
import edu.fit.cs.computernetworks.utils.SimpleLogger;
import edu.fit.cs.computernetworks.utils.Tuple;

public abstract class AbstractNetworkNode<T extends Node> {
	
	protected enum Transmit {
		RECEIVE, SEND
	}
	
	protected int ident = 0;
	
	protected final T descriptor;
	protected final Topology topology;
	
	protected final SimpleLogger logger;
	
	public AbstractNetworkNode(final Topology topo, final T descriptor) {
		this.descriptor = descriptor;
		this.topology = topo;
		this.logger = new SimpleLogger(descriptor.id);
	}
	
	public abstract int getLocalMTU(final String localIp);
	public abstract byte[] getLocalMAC(final String localIp);
	public abstract void transport(final byte[] payload, final Transmit transmit, final Address addr);
	
	public  void networkLayer(final byte[] payload, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND: {
			logger.log("network-layer send");
			final byte[] srcMac = getLocalMAC(addr.getSourceAddress());
			final byte[] destIp = NetUtils.intIpToByteArray(addr.destAddressToInt());
			final byte[] nextHop = descriptor.nextHopTo(destIp);
			final byte[] destMac = NetUtils.macToByteArray(topology.arpResolve(nextHop));
			final IPPacket pkg = new IPPacket(ident++, addr.sourceAddressToInt(), addr.destAddressToInt());
			
			pkg.setData(payload);
			linkLayer(pkg.toByteArray(), transmit, Tuple.of(srcMac, destMac));
			
			break;
		}
		case RECEIVE: {
			logger.log("network-layer receive");
			final IPPacket pkg = IPPacket.fromByteArray(payload);
			// TODO error correction/detection?
			final byte[] data = pkg.getData();
			final String sourceIPAddress = NetUtils.intIPToString(pkg.getSourceIPAddress());
			final String destIPAddress = NetUtils.intIPToString(pkg.getDestIPAddress());
			
			transport(data, transmit, new Address(sourceIPAddress, destIPAddress));
			
			//networkLayer(data, transmit, new Address(sourceIPAddress, destIPAddress));
			
			break;
		}
		default: // no-op
		}
	}
	
	public void linkLayer(final byte[] payload, final Transmit transmit, final Tuple<byte[], byte[]> macAddresses) {
		switch (transmit) {
		case SEND:
			logger.log("link-layer send");
			final byte[] srcMac = macAddresses._1;
			final byte[] destMac = macAddresses._2;
			final EthernetFrame destFrame = new EthernetFrame(srcMac, destMac);
			
			destFrame.setPayload(payload);
			physicalLayer(destFrame.toByteArray(), transmit, destMac);
			
			break;
		case RECEIVE:
			logger.log("link-layer receive");
			final EthernetFrame srcFrame = EthernetFrame.from(payload);
			final int crc = srcFrame.getCrc32();
			if (!srcFrame.validate(crc)) {
				logger.error("CRC check failed");
			}
			
			// Deliver payload to next layer
			networkLayer(srcFrame.getPayload(), transmit, null);
			
			break;
		}
	}
	
	public void physicalLayer(final byte[] payload, final Transmit transmit, final byte[] macAddr) {
		switch (transmit) {
		case SEND:
			logger.log("physical-layer send");
			final AbstractNetworkNode<? extends Node> nextHop = topology.machineFor(macAddr);
			nextHop.physicalLayer(payload, Transmit.RECEIVE, null);
			
			break;
		case RECEIVE:
			logger.log("physical-layer receive");
			// Just send it on to the link-layer
			linkLayer(payload, transmit, null);
			
			break;
		default: // no-op
		}
	}
	
}
