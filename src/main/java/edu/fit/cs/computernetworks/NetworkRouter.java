package edu.fit.cs.computernetworks;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;
import edu.fit.cs.computernetworks.utils.Tuple;

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
	 * and a new IP packet is constructed and sent to the 
	 * {@link #linkLayer(byte[], edu.fit.cs.computernetworks.AbstractNetworkNode.Transmit, Tuple)}
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
			logger.error("Header checksum mismatch! Dropping package...");
		}
		
		// Find correct network interface to write new package
		final IP destIp = NetUtils.wrap(pkg.getDestIPAddress());
		final Port interf = descriptor.getPortForDestinationIP(destIp);
		if (interf == null) {
			logger.log("Unable to locate interface for destination IP: " + destIp.toString());
			return;
		}
		
		// Extract source/destination MACs and construct new package
		final byte[] srcMac = getLocalMAC(NetUtils.wrap(interf.ip));
		final byte[] destMac = NetUtils.macToByteArray(topology.arpResolve(destIp));
		final IPPacket newPkg = new IPPacket(ident++, NetUtils.wrap(interf.ip).toInt(), destIp.toInt());
		newPkg.setPayload(pkg.getPayload());

		// Send to destination MAC
		logger.log("network-layer send");
		linkLayer(newPkg.toByteArray(), Transmit.SEND, Tuple.of(srcMac, destMac));
	}
	

	@Override
	public int getLocalMTU(final IP localIp) {
		for (final Port port : descriptor.ports) {
			if (port.ip.equals(localIp.toString())) {
				return port.mtu;
			}
		}
		
		return DEFAULT_MTU;
	}
	
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
