package edu.fit.cs.computernetworks;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.logging.Logger;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.EthernetFrame;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

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
	
	private final Logger logger;
	
	public AbstractNetworkNode(final Topology topo, final T descriptor) {
		this.descriptor = descriptor;
		this.topology = topo;
		this.logger = Logger.getLogger(descriptor.id);
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
	 * <b>Sending payload</b>
	 * <p>When sending a given payload, it slices the payload into many segments based on the payload size. 
	 * First it will check the length of the payload to make sure it needs to be sliced to MTU's size or not.
	 * If the payload size is more than the MTU's size then it will slice the segments considering the header length.
	 * These segments has TCP header along with data. Source and destination ports are included in the header.
	 * SYN and FIN flags are used to identify segments starting and ending point.
	 * This payload will be sent to network layer for adding IP header and route this segment to right node.
	 * Sending parameters would be {@network #networkLayer(byte[], Transmit, Address)} method.</p>
	 * 
	 * <b>Receiving payloads</b>
	 * <p>When receiving a payload, this method will convert the payload into segments.
	 * It will then extract the chechsum to make sure segment is valid, if it's not valid it drops. 
	 * If not, it keeps accumulating the segments into a buffer. along with sequence number.
	 * Then it will check for the FIN flag in the header to confirm that that's the last segment.
	 * If it's not FIN (it must be SYN), then it keeps assembling the segments to build a complete payload.
	 * Once the payload is built, it will be sent to application layer for creating a file in destination node.</p>
	 * 
	 * @param payload
	 * @param transmit
	 * @param addr
	 * 
	 */
	public abstract void transportLayer(final byte[] payload, final Transmit transmit, final Address addr);
	
	/**
	 * Network Layer
	 * 
	 * <b>Sending payloads</b>
	 * <p>When sending a given payload, this method will use the provided {@link Address} 
	 * parameter to determine the next hop and its MAC address. It will then package the
	 * payload in an {@link IPPacket} with source and destination addresses set before
	 * handing it off to the {@link #linkLayer(byte[], Transmit, Address)} method.</p>
	 * 
	 * <b>Receiving payloads</b>
	 * <p>When receiving a payload, this method will convert the payload into an {@link IPPacket}.
	 * It will then extract the source and destination IP addresses from the header and 
	 * construct a new {@link Address} object. The address object along with the extracted
	 * payload will be handed off to the {@link #transportLayer(byte[], Transmit, Address)}
	 * method. Also note that the method will verify the header checksum. If there is a
	 * mismatch, the package will be dropped.</p>
	 * 
	 * @param payload
	 * @param transmit
	 * @param addr
	 * 
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
			logger.info(format("Send (IP header=%s)", Arrays.toString(pkg.getHeader())));
			linkLayer(pkg.toByteArray(), transmit, addr);
			
			break;
		}
		case RECEIVE: {			
			// Reconstruct IP packet and validate header checksum
			final IPPacket pkg = IPPacket.from(payload);
			logger.info(format("Received (IP header=%s)", Arrays.toString(pkg.getHeader())));
			
			final int checksum = pkg.getHeaderChecksum();
			if (!pkg.validate(checksum)) {
				logger.info("IP header checksum mismatch! Dropping package...");
				return;
			}
			
			// Extract payload and construct source/destination address field
			final byte[] data = pkg.getPayload();
			final IP sourceIPAddress = NetUtils.wrap(pkg.getSourceIPAddress());
			final IP destIPAddress = NetUtils.wrap(pkg.getDestIPAddress());
			
			// Deliver payload to transport-layer
			transportLayer(data, transmit, new Address(sourceIPAddress.toString(), destIPAddress.toString()));
			
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
			logger.info(format("Send (Ethernet header=%s)", Arrays.toString(destFrame.getHeader())));
			physicalLayer(destFrame.toByteArray(), transmit, addr);
			
			break;
		case RECEIVE:
			// Reconstruct Ethernet frame and validate CRC
			final EthernetFrame srcFrame = EthernetFrame.from(payload);
			logger.info(format("Received (Ethernet header=%s)", Arrays.toString(srcFrame.getHeader())));

			final int crc = srcFrame.getCrc32();
			if (!srcFrame.validate(crc)) {
				logger.severe("CRC check failed! Dropping packet..");
				return;
			}
			
			// Make sure that the packet is for us
			final byte[] destMac = srcFrame.getDestinationMac();
			if (!matchesInterface(destMac)) {
				logger.info(format("Payload with destination MAC '%s' does not belong to me!", NetUtils.byteArrayToMac(destMac)));
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
	 * <p>When sending payloads, this method will find all nodes connected to the destination
	 * network. The payload is then written to all nodes except the sender to simulate writing
	 * the payload on the 'wire'. This is achieved by basically calling the corresponding method
	 * on each node ({@link #physicalLayer(byte[], Transmit, Address)}).</p>
	 * 
	 * <b>Receiving payloads</b>
	 * <p>When receiving payloads, this method will only send the payload up to the next layer.
	 * In this case, to {@link #linkLayer(byte[], Transmit, Address)}.</p>
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
			logger.info(format("Send to network '%s'", network));
			for (final AbstractNetworkNode<? extends Node> node : topology.nodesForNetwork(network)) {
				if (!node.equals(this)) {
					node.physicalLayer(payload, Transmit.RECEIVE, null);
				}
			}
			
			break;
		case RECEIVE:
			// Deliver payload to link-layer
			logger.info("Received packet");
			linkLayer(payload, transmit, null);
			
			break;
		default: // no-op
		}
	}
	
}
