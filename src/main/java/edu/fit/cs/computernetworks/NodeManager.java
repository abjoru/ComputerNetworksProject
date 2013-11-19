package edu.fit.cs.computernetworks;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.fit.cs.computernetworks.topology.Host;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.Topology;

public class NodeManager {
	
	private final Topology topology;
	private final Map<String, NetworkNode<? extends Node>> macToNetworkNodeTable;
	
	public NodeManager(final URL topologyUrl) {
		this(createTopology(topologyUrl));
	}
	
	public NodeManager(final Topology topology) {
		this.topology = topology;
		this.macToNetworkNodeTable = new HashMap<>();
		
		buildNodeTable();
	}

	public NetworkNode<? extends Node> route(final String mac) {
		synchronized (macToNetworkNodeTable) {
			return macToNetworkNodeTable.get(mac);
		}
	}
	
	public Host hostByName(final String hostname) {
		synchronized (topology) {
			final Node node = topology.nodeById(hostname);
			if (!(node instanceof Host)) {
				throw new RuntimeException(String.format("hostname (%s) does not resolve to a host", hostname));
			}
			
			return (Host) node;
		}
	}
	
	private void buildNodeTable() {
		for (final Node node : topology.getNodes()) {
			if (node instanceof Host) {
				final Host host = (Host) node;
				macToNetworkNodeTable.put(host.mac, new NetworkHost(topology, host, null, this));
			} else if (node instanceof Router) {
				final Router router = (Router) node;
				final NetworkRouter nr = new NetworkRouter(topology, router, this);
				for (final Port port : ((Router) node).ports) {
					macToNetworkNodeTable.put(port.getMac(), nr);
				}
			}
		}
	}

	private static Topology createTopology(URL topologyUrl) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(topologyUrl, Topology.class);
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
