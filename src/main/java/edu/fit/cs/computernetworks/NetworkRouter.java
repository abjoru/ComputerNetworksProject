package edu.fit.cs.computernetworks;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;
import edu.fit.cs.computernetworks.utils.Tuple;

public class NetworkRouter extends AbstractNetworkNode<Router> {
	
	private static final int DEFAULT_MTU = 1400;

	public NetworkRouter(final Topology topology, final Router descriptor) {
		super(topology, descriptor);
	}
	
	@Override
	public void transport(final byte[] payload, final Transmit transmit, final Address addr) {
		// no-op
		logger.error("TRANSPORT WAS CALLED ON A ROUTER!!");
	}
	
	@Override
	public void networkLayer(final byte[] payload, final Transmit transmit, final Address addr) {
		assert transmit == Transmit.RECEIVE;
		
		final IPPacket pkg = IPPacket.fromByteArray(payload);
		final IP destIp = NetUtils.wrap(pkg.getDestIPAddress());
		final Port interf = descriptor.getPortForDestinationIP(destIp);
		if (interf == null) {
			logger.log("Unable to locate interface for destination IP: " + destIp.toString());
			return;
		}
		
		final byte[] srcMac = getLocalMAC(NetUtils.wrap(interf.ip));
		final byte[] destMac = NetUtils.macToByteArray(topology.arpResolve(destIp));
		final IPPacket newPkg = new IPPacket(ident++, NetUtils.wrap(interf.ip).toInt(), destIp.toInt());
		
		newPkg.setData(pkg.getData());
		
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
