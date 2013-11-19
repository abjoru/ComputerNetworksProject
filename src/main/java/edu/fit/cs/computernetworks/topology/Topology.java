package edu.fit.cs.computernetworks.topology;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.fit.cs.computernetworks.AbstractNetworkNode;
import edu.fit.cs.computernetworks.NetworkHost;
import edu.fit.cs.computernetworks.NetworkRouter;

public class Topology {
	
	public List<Node> nodes;
	public List<MACTableEntry> macTable;
	
	@JsonIgnore
	private Map<String, AbstractNetworkNode<? extends Node>> arpTable; 

	/**
	 * Simulates a host name resolution. Translate a given hostname into
	 * it's IP address.
	 * 
	 * @param hostname
	 * @return
	 */
	public Node resolve(final String hostname) {
		for (final Node node : nodes) {
			if (node.id.equals(hostname)) {
				return node;
			}
		}
		
		return null;
	}
	
	/**
	 * Simulates an ARP hardware address lookup. Translates a given
	 * IP address into it's MAC equivalent.
	 * 
	 * @param ipAddress
	 * @return
	 */
	public String arpResolve(final String ipAddress) {
		for (final MACTableEntry e : macTable) {
			if (e.ip.equals(ipAddress)) {
				return e.mac;
			}
		}
		
		return null;
	}
	
	/**
	 * TODO find better name of method
	 * 
	 * @param macAddress
	 * @return
	 */
	public AbstractNetworkNode<? extends Node> machineFor(final String macAddress) {
		return arpTable.get(macAddress);
	}
	
	/**
	 * Builds the ARP table, meaning that each network node is created and
	 * placed in the table. 
	 * 
	 * TODO this method should start each thread and also configure the 
	 * monitor folder for them..
	 */
	public void buildARP() {
		for (final Node node : nodes) {
			if (node instanceof Host) {
				final Host host = (Host) node;
				arpTable.put(host.mac, new NetworkHost(this, host));
			} else if (node instanceof Router) {
				final Router router = (Router) node;
				final NetworkRouter nRouter = new NetworkRouter(this, router);
				for (final Port port : router.ports) {
					arpTable.put(port.mac, nRouter);
				}
			}
		}
	}
	
}
