package edu.fit.cs.computernetworks.topology;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TopologyTest {
	private URL path = getClass().getResource("/topology.json");
	
	@Test
	public void constructTopology() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Topology t = mapper.readValue(path, Topology.class);
		Assert.assertNotNull(t);
		Assert.assertEquals(4, t.getNodes().size());
		
		Host a = (Host) t.nodeById("A");
		Assert.assertNotNull(a);
		Assert.assertEquals("A", a.id);
		Assert.assertEquals("10.10.20.1", a.ip);
		Assert.assertEquals("255.255.255.0", a.mask);
		Assert.assertEquals("00:B0:D0:86:BB:F7", a.mac);
		Assert.assertEquals(1400, a.mtu);
		Assert.assertEquals(1, a.getLinks().size());
	}
	
}
