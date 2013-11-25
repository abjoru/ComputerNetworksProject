package edu.fit.cs.computernetworks;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.fit.cs.computernetworks.topology.Topology;

public class Simulator {
	
	public static void main(final String[] args) {
		final Topology topology = createTopology("/topology2.json");
		topology.buildARP();
		
		// Override observable path
		if (args.length == 1) {
			topology.setRootPath(args[0]);
		}
		
		// Start hosts
		for (final Thread t : topology.threads) {
			t.start();
		}
	}

	private static Topology createTopology(String topo) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(Simulator.class.getResource(topo), Topology.class);
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
