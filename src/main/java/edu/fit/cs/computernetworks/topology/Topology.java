package edu.fit.cs.computernetworks.topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Topology {
	
	private List<Node> nodes;
	
	public List<Node> getNodes() {
		return nodes;
	}
	
	public Node nodeById(final String id) {
		for (final Node node : nodes) {
			if (node.getId().equals(id)) {
				return node;
			}
		}
		
		return null;
	}
	
	public List<Node> connectionsByNode(final Node n) {
		final List<Node> res = new ArrayList<>();
		for (final String l : n.getLinks()) {
			res.add(nodeById(l));
		}
		
		return res;
	}
	
	public List<Node> connectionsByNode(final Node n, final Port local) {
		final byte[] netAddr = local.toNetworkAddress();
		final List<Node> res = new ArrayList<>();
		
		for (final String l : n.getLinks()) {
			final Node link = nodeById(l);
			
			for (final Port p : link.getPorts()) {
				final byte[] nodeNetAddr = p.toNetworkAddress();
				if (Arrays.equals(netAddr, nodeNetAddr)) {
					res.add(link);
				}
			}
		}
		
		return res;
	}

}
