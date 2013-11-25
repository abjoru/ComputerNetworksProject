package edu.fit.cs.computernetworks;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import edu.fit.cs.computernetworks.AbstractNetworkNode.Transmit;
import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.EthernetFrame;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.topology.Port;
import edu.fit.cs.computernetworks.topology.Router;
import edu.fit.cs.computernetworks.topology.RoutingEntry;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class NetworkRouterTest {
	
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void sendPacket() {
		NetworkRouter dest = mock(NetworkRouter.class);
		
		Router r1 = new Router();
		r1.id = "RA";
		r1.ports = new ArrayList<>();
		r1.ports.add(new Port("10.0.0.1", "255.255.255.0", "00:B0:D0:86:BB:F7", 1400));
		r1.routing = new ArrayList<>();
		r1.routing.add(new RoutingEntry("10.0.0.2", "10.0.0.2"));
		
		Router r2 = new Router();
		r2.id = "RB";
		r2.ports = new ArrayList<>();
		r2.ports.add(new Port("10.0.0.2", "255.255.255.0", "B0:00:D0:86:BB:F7", 1400));
		r2.routing = new ArrayList<>();
		r2.routing.add(new RoutingEntry("10.0.0.1", "10.0.0.1"));
		
		Topology topo = mock(Topology.class);
		NetworkRouter router = new NetworkRouter(topo, r1);

		byte[] srcMac = NetUtils.macToByteArray("00:B0:D0:86:BB:F7");
		byte[] destMac = NetUtils.macToByteArray("B0:00:D0:86:BB:F7");
		when(topo.arpResolve(eq("10.0.0.1"))).thenReturn("00:B0:D0:86:BB:F7");
		when(topo.arpResolve(eq("10.0.0.2"))).thenReturn("B0:00:D0:86:BB:F7");
		when(topo.machineFor(eq(srcMac))).thenReturn((AbstractNetworkNode) router);
		when(topo.machineFor(eq(destMac))).thenReturn((AbstractNetworkNode) dest);

		router.networkLayer("This is the payload".getBytes(), Transmit.SEND, new Address("10.0.0.1", "10.0.0.2"));
		
		ArgumentCaptor<byte[]> pkg = ArgumentCaptor.forClass(byte[].class);
		verify(dest).physicalLayer(pkg.capture(), eq(Transmit.RECEIVE), isNull(byte[].class));
		
		byte[] packet = pkg.getValue();
		Assert.assertNotNull(packet);
		
		EthernetFrame frame = EthernetFrame.from(packet);
		byte[] ipPkg = frame.getPayload();
		byte[] payload = IPPacket.fromByteArray(ipPkg).getData();
		Assert.assertEquals("This is the payload", new String(payload));
	}
	
}
