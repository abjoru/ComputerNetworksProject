package edu.fit.cs.computernetworks.topology;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.fit.cs.computernetworks.utils.IP;

public class TopologyTest {
	private URL path = getClass().getResource("/topology2.json");
	
	@Test
	public void constructTopology2() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Topology t = mapper.readValue(path, Topology.class);
		Assert.assertNotNull(t);
		Assert.assertEquals(4, t.nodes.size());
		
		Host a = (Host) t.resolve("A");
		Assert.assertNotNull(a);
		Assert.assertEquals("A", a.id);
		Assert.assertEquals("10.10.20.1", a.ip);
		Assert.assertEquals("255.255.255.0", a.mask);
		Assert.assertEquals("00:B0:D0:86:BB:F7", a.mac);
		Assert.assertEquals(1400, a.mtu);
		Assert.assertEquals("10.10.20.2", a.gateway);
		
		IP netAddr = a.routing.get(0).networkAddress();
		byte[] networkAddress = netAddr.toByteArray();
		Assert.assertEquals(192, networkAddress[0] & 0xff);
		Assert.assertEquals(168, networkAddress[1] & 0xff);
		Assert.assertEquals(25, networkAddress[2] & 0xff);
		Assert.assertEquals(0, networkAddress[3] & 0xff);
	}
	
}
