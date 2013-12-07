package edu.fit.cs.computernetworks.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.fit.cs.computernetworks.AbstractNetworkNode;
import edu.fit.cs.computernetworks.NetworkHost;
import edu.fit.cs.computernetworks.NetworkRouter;
import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class Topology {
	
	public List<Node> nodes;
	public List<MACTableEntry> macTable;
	
	@JsonIgnore
	public List<Thread> threads;
	
	@JsonIgnore
	private Map<String, AbstractNetworkNode<? extends Node>> arpTable;
	
	public Topology() {
		this.threads = new ArrayList<>();
		this.arpTable = new HashMap<String, AbstractNetworkNode<? extends Node>>();
	}

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
	public String arpResolve(final IP ipAddress) {
		for (final MACTableEntry e : macTable) {
			if (e.ip.equals(ipAddress.toString())) {
				return e.mac;
			}
		}
		
		return null;
	}
	
	public AbstractNetworkNode<? extends Node> machineFor(final byte[] macAddress) {
		for (final String mac : arpTable.keySet()) {
			if (Arrays.equals(NetUtils.macToByteArray(mac), macAddress)) {
				return arpTable.get(mac);
			}
		}
		
		return null;
	}
	
	/**
	 * Returns all nodes belonging to the given network.
	 * 
	 * @param network
	 * @return
	 */
	public Collection<AbstractNetworkNode<? extends Node>> nodesForNetwork(final IP network) {
		final Set<AbstractNetworkNode<? extends Node>> results = new HashSet<>();
		for (final AbstractNetworkNode<? extends Node> node : arpTable.values()) {
			if (node.belongsToNetwork(network)) 
				results.add(node);
		}
		
		return results;
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
				final NetworkHost hostNode = new NetworkHost(this, host);
				threads.add(new Thread(hostNode));
				arpTable.put(host.mac, hostNode);
			} else if (node instanceof Router) {
				final Router router = (Router) node;
				final NetworkRouter nRouter = new NetworkRouter(this, router);
				for (final Port port : router.ports) {
					arpTable.put(port.mac, nRouter);
				}
			}
		}
	}
	
	public void setRootPath(final String path) {
		for (final Node node : nodes) {
			if (node instanceof Host) {
				((Host) node).rootPath = path;
			}
		}
	}
	
}
