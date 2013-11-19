package edu.fit.cs.computernetworks.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.fit.cs.computernetworks.topology.Host;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.IPUtils;
import edu.fit.cs.computernetworks.utils.Tuple;

/**
 * Find MAC addr for next hop and fragment the packet.
 * Perform error checking, etc..
 * 
 * @author abjoru
 *
 */
public class DataLinkLayer implements OSILayer<IPPacket> {
	
	private int identifier = 0;
	private Node nodeDescriptor;
	
	private final Topology topology;
	private final Map<String, String> macTable;
	
	public DataLinkLayer(final Topology topology, final Node descriptor) {
		this.topology = topology;
		this.nodeDescriptor = descriptor;
		this.macTable = new HashMap<>();
		
		initializeMacTable();
	}

	private void initializeMacTable() {
		// Construct mac lookup table from node neighbors
		for (final Node directNeighbors : topology.connectionsByNode(nodeDescriptor)) {
			if (directNeighbors instanceof Host) {
				final Host host = (Host) directNeighbors;
				macTable.put(host.ip, host.mac);
			} else {
				final Router router = (Router) directNeighbors;
				for (final Port p : router.ports) {
					macTable.put(p.getIp(), p.getMac());
				}
			}
		}
	}
	
	public Tuple<String, Collection<IPPacket>> handleSend(final byte[] data, final Address addr) {
		final String destMacAddress = macTable.get(addr.getDestinationAddress());
		final byte[] destAddr = IPUtils.ipToByteArray(addr.getDestinationAddress());
		final Tuple<String, Integer> localPort = findLocalPort(destAddr);
		
		if (localPort._2 < data.length) {
			// TODO need to fragment the package!
			return Tuple.of(destMacAddress, (Collection<IPPacket>) new ArrayList<IPPacket>(0));
		} else {
			final IPPacket pkg = new IPPacket(identifier++, addr.sourceAddressToInt(), addr.destAddressToInt());
			pkg.setData(data);
			return Tuple.of(destMacAddress, (Collection<IPPacket>) Arrays.asList(pkg));
		}
	}
	
	private Tuple<String, Integer> findLocalPort(final byte[] dest) {
		if (nodeDescriptor instanceof Host) {
			final Host h = (Host) nodeDescriptor;
			return Tuple.of(h.mac, Integer.valueOf(h.mtu));
		} else {
			final Router r = (Router) nodeDescriptor;
			for (final Port p : r.ports) {
				final byte[] mask = IPUtils.ipToByteArray(p.getMask());
				final byte[] networkAddr = IPUtils.networkAddress(dest, mask);
				if (p.matches(networkAddr)) {
					return Tuple.of(p.getMac(), Integer.valueOf(p.getMtu()));
				}
			}
		}
		
		return null;
	}
	
	@Override
	public IPPacket handleReceive(byte[] packet) {
		return IPPacket.fromByteArray(packet);
	}

}
