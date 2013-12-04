package edu.fit.cs.computernetworks;

import java.util.Arrays;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.EthernetFrame;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;
import edu.fit.cs.computernetworks.utils.SimpleLogger;
import edu.fit.cs.computernetworks.utils.Tuple;

/**
 * Base class for the two possible network node types; router and host.
 * 
 * @author Andreas Bjoru
 *
 * @param <T> descriptor type (extending Node)
 */
public abstract class AbstractNetworkNode<T extends Node> {
	
	/** Describes either send or receive action */
	protected enum Transmit { RECEIVE, SEND }
	
	protected int ident = 0;
	
	protected final T descriptor;
	protected final Topology topology;
	
	// shared logger
	protected final SimpleLogger logger;
	
	public AbstractNetworkNode(final Topology topo, final T descriptor) {
		this.descriptor = descriptor;
		this.topology = topo;
		this.logger = new SimpleLogger(descriptor.id);
	}
	
	/**
	 * Returns the network address of this node. The interface address
	 * argument can be used in case the node has multiple network 
	 * interfaces (such as for Routers).
	 * 
	 * @param optionalIP address of interface.
	 * @return
	 */
	public abstract IP networkAddress(final IP ifAddress);
	
	/**
	 * Tests whether or not this node belongs to a given network address.
	 * 
	 * @param networkAddr
	 * @return
	 */
	public abstract boolean belongsToNetwork(final IP networkAddr);
	
	/**
	 * Tests whether the <code>macAddr</code> matches any of the network
	 * interfaces of this node.
	 * 
	 * @param macAddr
	 * @return
	 */
	public abstract boolean matchesInterface(final byte[] macAddr);
	
	/**
	 * Returns the local MTU for this node. The local IP argument 
	 * is provided for router nodes that may have more than one 
	 * network interface.  
	 * 
	 * @param localIp interface IP address.
	 * @return
	 */
	public abstract int getLocalMTU(final IP localIp);
	
	/**
	 * Returns the local MAC address for this node. The local IP
	 * argument is provided for router nodes that may have more 
	 * than one network interface.
	 * 
	 * @param localIp interface IP address.
	 * @return
	 */
	public abstract byte[] getLocalMAC(final IP localIp);
	
	/**
	 * Transport Layer
	 * 
	 * @param payload
	 * @param transmit
	 * @param addr
	 */
	public abstract void transport(final byte[] payload, final Transmit transmit, final Address addr);
	
	/**
	 * Network Layer
	 * 
	 * <b>Sending payloads</b>
	 * <p>When sending a given payload, this method will use the provided {@link Address} 
	 * parameter to determine the next hop and its MAC address. It will then package the
	 * payload in an {@link IPPacket} with source and destination addresses set before
	 * handing it off to the {@link #linkLayer(byte[], Transmit, Tuple)} method.</p>
	 * 
	 * <b>Receiving payloads</b>
	 * <p>When receiving a payload, this method will convert the payload into an {@link IPPacket}.
	 * It will then extract the source and destination IP addresses from the header and 
	 * construct a new {@link Address} object. The address object along with the extracted
	 * payload will be handed off to the {@link #transport(byte[], Transmit, Address)}
	 * method. Also note that the method will verify the header checksum. If there is a
	 * mismatch, the package will be dropped.</p>
	 * 
	 * @param payload
	 * @param transmit
	 * @param addr
	 * 
	 * @see Node#nextHopTo(IP)
	 * @see Topology#arpResolve(IP)
	 * @see IPPacket#validate(int)
	 */
	public  void networkLayer(final byte[] payload, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND: {
			// Find next hop and prepare source/destination MACs
			final IP source = addr.getSourceAddress();
			final IP destination = addr.getDestinationAddress();
			final IP nextHop = descriptor.nextHopTo(destination);
			
			// Update address holder
			addr.setSourceMac(getLocalMAC(source));
			addr.setDestMac(NetUtils.macToByteArray(topology.arpResolve(nextHop)));
			
			// Construct IP package and set payload
			final IPPacket pkg = new IPPacket(ident++, source.toInt(), destination.toInt());
			pkg.setPayload(payload);
			
			// Deliver to link-layer
			logger.log("NETWORK-LAYER: send (IP header=%s)", Arrays.toString(pkg.getHeader()));
			linkLayer(pkg.toByteArray(), transmit, addr);
			
			break;
		}
		case RECEIVE: {			
			// Reconstruct IP packet and validate header checksum
			final IPPacket pkg = IPPacket.from(payload);
			logger.log("NETWORK-LAYER: received (IP header=%s)", Arrays.toString(pkg.getHeader()));
			
			final int checksum = pkg.getHeaderChecksum();
			if (!pkg.validate(checksum)) {
				logger.error("NETWORK-LAYER: IP header checksum mismatch! Dropping package...");
				return;
			}
			
			// Extract payload and construct source/destination address field
			final byte[] data = pkg.getPayload();
			final IP sourceIPAddress = NetUtils.wrap(pkg.getSourceIPAddress());
			final IP destIPAddress = NetUtils.wrap(pkg.getDestIPAddress());
			
			// Deliver payload to transport-layer
			transport(data, transmit, new Address(sourceIPAddress.toString(), destIPAddress.toString()));
			
			break;
		}
		default: // no-op
		}
	}
	
	/**
	 * Link Layer
	 * 
	 * <b>Sending payloads</b>
	 * <p>When sending payloads, this method will construct an {@link EthernetFrame} with payload,
	 * source, and destination MAC addresses. The frame is then delivered to 
	 * {@link #physicalLayer(byte[], Transmit, byte[])}</p>
	 * 
	 * <b>Receiving payloads</b>
	 * <p>When receiving payloads, this method will first reconstruct the {@link EthernetFrame}
	 * before verifying the frame's CRC. If the CRC is valid, the frame's payload is delivered
	 * to the {@link #networkLayer(byte[], Transmit, Address)}.</p>
	 * 
	 * @param payload
	 * @param transmit
	 * @param macAddresses
	 * 
	 * @see EthernetFrame#validate(int)
	 */
	public void linkLayer(final byte[] payload, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND:
			// Extract source/destination MACs and construct Ethernet frame
			final EthernetFrame destFrame = new EthernetFrame(addr.getSourceMac(), addr.getDestMac());
			destFrame.setPayload(payload);
			
			// Deliver to physical-layer
			logger.log("LINK-LAYER: send (Ethernet header=%s)", Arrays.toString(destFrame.getHeader()));
			physicalLayer(destFrame.toByteArray(), transmit, addr);
			
			break;
		case RECEIVE:
			// Reconstruct Ethernet frame and validate CRC
			final EthernetFrame srcFrame = EthernetFrame.from(payload);
			logger.log("LINK-LAYER: received (Ethernet header=%s)", Arrays.toString(srcFrame.getHeader()));

			final int crc = srcFrame.getCrc32();
			if (!srcFrame.validate(crc)) {
				logger.error("LINK-LAYER: CRC check failed! Dropping packet..");
				return;
			}
			
			// Make sure that the packet is for us
			final byte[] destMac = srcFrame.getDestinationMac();
			if (!matchesInterface(destMac)) {
				logger.log("LINK-LAYER: Payload with destination MAC '%s' does not belong to me!", NetUtils.byteArrayToMac(destMac));
				return;
			}
			
			// Deliver payload to network-layer
			networkLayer(srcFrame.getPayload(), transmit, null);
			
			break;
		}
	}
	
	/**
	 * Physical Layer
	 * 
	 * <b>Sending payloads</b>
	 * <p>When sending payloads, this method will first find the physical machine (or thread)
	 * based on the destination MAC address. Once the machine has been found, the payload is
	 * delivered directly to that machines {@link #physicalLayer(byte[], Transmit, byte[])}.</p>
	 * 
	 * <b>Receiving payloads</b>
	 * <p>When receiving payloads, this method will only send the payload up to the next layer.
	 * In this case, to {@link #linkLayer(byte[], Transmit, Tuple)}.</p>
	 * 
	 * @param payload
	 * @param transmit
	 * @param addr
	 * 
	 * @see Topology#machineFor(byte[])
	 */
	public void physicalLayer(final byte[] payload, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND:
			// Find network and write payload bytes on 'wire'
			final IP network = networkAddress(addr.getSourceAddress());
			logger.log("PHYSICAL-LAYER: send to network '%s'", network);
			for (final AbstractNetworkNode<? extends Node> node : topology.nodesForNetwork(network)) {
				if (!node.equals(this)) {
					node.physicalLayer(payload, Transmit.RECEIVE, null);
				}
			}
			
			break;
		case RECEIVE:
			logger.log("PHYSICAL-LAYER: receive");
			
			// Deliver payload to link-layer
			linkLayer(payload, transmit, null);
			
			break;
		default: // no-op
		}
	}
	
}
