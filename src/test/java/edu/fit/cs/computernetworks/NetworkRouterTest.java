package edu.fit.cs.computernetworks;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.fit.cs.computernetworks.AbstractNetworkNode.Transmit;
import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.RoutingEntry;
import edu.fit.cs.computernetworks.topology.Topology;

public class NetworkRouterTest {
	
	@Test
	@SuppressWarnings("unchecked")
	public void sendPacket() {
		NetworkRouter dest = mock(NetworkRouter.class);
		
		Router r1 = new Router();
		r1.id = "RA";
		r1.ports = new ArrayList<>();
		r1.ports.add(new Port("10.0.0.1", "255.255.255.0", "00:B0:D0:86:BB:F7", 1400));
		r1.routing = new ArrayList<>();
		r1.routing.add(new RoutingEntry("10.0.0.2", "10.0.0.2"));
		
		Router r2 = new Router();
		r2.id = "B";
		r2.ports = new ArrayList<>();
		r2.ports.add(new Port("10.0.0.2", "255.255.255.0", "B0:00:D0:86:BB:F7", 1400));
		r2.routing = new ArrayList<>();
		r2.routing.add(new RoutingEntry("10.0.0.1", "10.0.0.1"));
		
		Topology topo = mock(Topology.class);
		NetworkRouter router = new NetworkRouter(topo, r1);

		when(topo.arpResolve(eq("10.0.0.1"))).thenReturn("00:B0:D0:86:BB:F7");
		when(topo.arpResolve(eq("10.0.0.2"))).thenReturn("B0:00:D0:86:BB:F7");
		when(topo.machineFor(eq("00:B0:D0:86:BB:F7"))).thenReturn((AbstractNetworkNode) router);
		when(topo.machineFor(eq("B0:00:D0:86:BB:F7"))).thenReturn((AbstractNetworkNode) dest);

		router.linkLayer("This is the payload".getBytes(), Transmit.SEND, new Address("10.0.0.1", "10.0.0.2"));
		
		ArgumentCaptor<byte[]> pkg = ArgumentCaptor.forClass(byte[].class);
		verify(dest).physicalLayer(pkg.capture(), eq(Transmit.RECEIVE), isNull(String.class));
		
		byte[] packet = pkg.getValue();
		Assert.assertNotNull(packet);
		
		final byte[] payload = IPPacket.fromByteArray(packet).getData();
		Assert.assertEquals("This is the payload", new String(payload));
	}
	
	private Topology loadTopology(String file) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(getClass().getResource(file), Topology.class);
		} catch (final IOException e) {
			Assert.fail("Unable to load topology");
			return null;
		}
	}

}
