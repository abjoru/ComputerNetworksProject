package edu.fit.cs.computernetworks;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import edu.fit.cs.computernetworks.NetworkNode.Transmit;
import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.Topology;

public class NetworkRouterTest {
	
	@Test
	@SuppressWarnings("unchecked")
	public void sendPacket() {
		NetworkRouter dest = Mockito.mock(NetworkRouter.class);
		
		Router r1 = new Router(); //("A", "router");
		r1.id = "A";
		r1.ports = new ArrayList<>();
		r1.ports.add(new Port("10.0.0.1", "255.255.255.0", "00:B0:D0:86:BB:F7", 1400));
		
		Router r2 = new Router();
		r2.id = "B";
		r2.ports = new ArrayList<>();
		r2.ports.add(new Port("10.0.0.2", "255.255.255.0", "B0:00:D0:86:BB:F7", 1400));
		
		Topology topo = Mockito.mock(Topology.class);
		Mockito.when(topo.connectionsByNode(Matchers.any(Node.class))).thenReturn(Arrays.asList((Node) r2));
		
		NodeManager mgr = Mockito.mock(NodeManager.class);
		Mockito.when(mgr.route(Matchers.anyString())).thenReturn((NetworkNode) dest);
		
		NetworkRouter router = new NetworkRouter(topo, r1, mgr);
		Address addr = new Address("10.0.0.1", "10.0.0.2");
		
		router.linkLayer("This is the payload".getBytes(), Transmit.SEND, addr);
		ArgumentCaptor<IPPacket> pkg = ArgumentCaptor.forClass(IPPacket.class);
		Mockito.verify(dest).physicalLayer(pkg.capture(), Matchers.eq(Transmit.RECEIVE), Matchers.isNull(String.class));
		
		Assert.assertEquals("This is the payload", new String(pkg.getValue().getData()));
	}
	
	@Test
	public void relayPacket() {
		
	}

}
