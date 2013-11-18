package edu.fit.cs.computernetworks.model;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Topology;

public class TopologyTest {
	private URL path = getClass().getResource("/topology.json");

	@Test
	public void constructTopology() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Topology t = mapper.readValue(path, Topology.class);
		Assert.assertNotNull(t);
		Assert.assertEquals(4, t.getNodes().size());
		
		Node a = t.nodeById("A");
		Assert.assertNotNull(a);
		Assert.assertEquals("A", a.getId());
		Assert.assertEquals("node", a.getType());
		Assert.assertEquals(1, a.getPorts().size());
		Assert.assertEquals(1, a.getLinks().size());
	}
	
	@Test
	public void connectionsByNode() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Topology t = mapper.readValue(path, Topology.class);
		Node r = t.nodeById("R");
		Assert.assertNotNull(r);
		Assert.assertEquals(2, r.getPorts().size());
		
		Port eth1 = r.getPorts().get(1);
		List<Node> links = t.connectionsByNode(r, eth1);
		Assert.assertEquals(2, links.size());
		Assert.assertTrue(containsLink("B", links));
		Assert.assertTrue(containsLink("C", links));
	}
	
	private boolean containsLink(final String id, final List<Node> nodes) {
		for (final Node n : nodes) {
			if (n.getId().equals(id)) {
				return true;
			}
		}
		
		return false;
	}
}
