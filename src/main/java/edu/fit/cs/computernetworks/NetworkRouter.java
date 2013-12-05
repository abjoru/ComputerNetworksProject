package edu.fit.cs.computernetworks;

import java.util.Arrays;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

/**
 * This class represents a network router. Most of the functionality is inherited
 * from the {@link AbstractNetworkNode} super class. 
 * 
 * @author Andreas Bjoru
 *
 */
public class NetworkRouter extends AbstractNetworkNode<Router> {
	// Default MTU if none can be found
	private static final int DEFAULT_MTU = 1400;

	public NetworkRouter(final Topology topology, final Router descriptor) {
		super(topology, descriptor);
	}
	
	@Override
	public void transport(final byte[] payload, final Transmit transmit, final Address addr) {
		// TODO we may need to call this layer on the router in the case where
		// network interface MTU differ between source and destination. I.e.
		// need to re-segment data to fit MTU of next hop.
		logger.error("TRANSPORT WAS CALLED ON A ROUTER!!");
	}
	
	/**
	 * The functionality of the network layer is overridden for routers since they will
	 * just route packages to the next network or host. Similar to the inherited version
	 * of this method, it will start by verifying the IP header checksum. If the verification
	 * fails, the package is dropped. Otherwise, the correct network interface is found and
	 * the MAC address of that interface is used as the new source MAC address. The 
	 * destination MAC address is then resolved through {@link Topology#arpResolve(IP)},
	 * and the payload is sent to the {@link #linkLayer(byte[], edu.fit.cs.computernetworks.AbstractNetworkNode.Transmit, Address)}
	 * method if the router.
	 * 
	 * @see Topology#arpResolve(IP)
	 * @see Router#getPortForDestinationIP(IP)
	 * @see IPPacket#validate(int)
	 */
	@Override
	public void networkLayer(final byte[] payload, final Transmit transmit, final Address addr) {
		assert transmit == Transmit.RECEIVE;
		
		// header checksum verification
		final IPPacket pkg = IPPacket.from(payload);
		final int checksum = pkg.getHeaderChecksum();
		if (!pkg.validate(checksum)) {
			logger.error("NETWORK-LAYER: Header checksum mismatch! Dropping package...");
		}
		
		// Find correct network interface to write new package
		final IP destIp = NetUtils.wrap(pkg.getDestIPAddress());
		final Port interf = descriptor.getPortForDestinationIP(destIp);
		if (interf == null) {
			logger.log("NETWORK-LAYER: Unable to locate interface for destination IP: " + destIp.toString());
			return;
		}
		
		// Extract source/destination MACs and construct new package
		final Address newAddr = new Address(interf.ip, destIp.toString());
		newAddr.setSourceMac(getLocalMAC(NetUtils.wrap(interf.ip)));
		newAddr.setDestMac(NetUtils.macToByteArray(topology.arpResolve(destIp)));

		// Send to destination MAC
		logger.log("NETWORK-LAYER: send (IP header=%s)", Arrays.toString(pkg.getHeader()));
		linkLayer(payload, Transmit.SEND, newAddr);
	}
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#networkAddress(edu.fit.cs.computernetworks.utils.IP)
	 */
	@Override
	public IP networkAddress(IP ifAddress) {
		final Port ethX = descriptor.getPortForDestinationIP(ifAddress);
		return (ethX == null) ? null : ethX.toNetworkAddress();
	}
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#belongsToNetwork(edu.fit.cs.computernetworks.utils.IP)
	 */
	@Override
	public boolean belongsToNetwork(IP networkAddr) {
		for (final Port port : descriptor.ports) {
			if (port.matches(networkAddr)) {
				return true;
			}
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#matchesInterface(byte[])
	 */
	@Override
	public boolean matchesInterface(byte[] macAddr) {
		for (final Port port : descriptor.ports) {
			final byte[] ifMac = NetUtils.macToByteArray(port.mac);
			if (Arrays.equals(macAddr, ifMac))
				return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#getLocalMTU(edu.fit.cs.computernetworks.utils.IP)
	 */
	@Override
	public int getLocalMTU(final IP localIp) {
		for (final Port port : descriptor.ports) {
			if (port.ip.equals(localIp.toString())) {
				return port.mtu;
			}
		}
		
		return DEFAULT_MTU;
	}
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#getLocalMAC(edu.fit.cs.computernetworks.utils.IP)
	 */
	@Override
	public byte[] getLocalMAC(final IP localIp) {
		for (final Port port : descriptor.ports) {
			if (port.ip.equals(localIp.toString())) {
				return NetUtils.macToByteArray(port.mac);
			}
		}
		
		return null;
	}

}
