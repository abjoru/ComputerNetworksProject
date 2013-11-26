package edu.fit.cs.computernetworks.model;

import org.junit.Assert;
import org.junit.Test;

import edu.fit.cs.computernetworks.utils.NetUtils;

public class IPPacketTest {
	
	@Test
	public void toFromByteArray() {
		IPPacket pkg = new IPPacket();
		pkg.setVersion((byte) 1);
		pkg.setDifferentiatedServices((byte) 6);
		pkg.setExplicitCongestionNotification((byte) 2);
		pkg.setTotalLength(5000);
		pkg.setIdentification(23);
		pkg.setFlags((byte) 2);
		pkg.setFragmentOffset(5);
		pkg.setTimeToLive((byte) 5);
		pkg.setProtocol((byte) 4);
		pkg.setHeaderChecksum(12345);
		pkg.setSourceIPAddress(234532);
		pkg.setDestIPAddress(546532);
		pkg.setData("This is some data".getBytes());
		
		byte[] bytes = pkg.toByteArray();
		Assert.assertNotNull(bytes);
		
		IPPacket spawned = IPPacket.fromByteArray(bytes);
		Assert.assertNotNull(spawned);
		Assert.assertEquals(1, spawned.getVersion());
		Assert.assertEquals(5, spawned.getInternetHeaderLength());
		Assert.assertEquals(6, spawned.getDifferentiatedServices());
		Assert.assertEquals(2, spawned.getExplicitCongestionNotification());
		Assert.assertEquals(5000, spawned.getTotalLength());
		Assert.assertEquals(23, spawned.getIdentification());
		Assert.assertEquals(2, spawned.getFlags());
		Assert.assertEquals(5, spawned.getFragmentOffset());
		Assert.assertEquals(5, spawned.getTimeToLive());
		Assert.assertEquals(4, spawned.getProtocol());
		Assert.assertEquals(12345, spawned.getHeaderChecksum());
		Assert.assertEquals(234532, spawned.getSourceIPAddress());
		Assert.assertEquals(546532, spawned.getDestIPAddress());
		Assert.assertArrayEquals("This is some data".getBytes(), spawned.getData());
	}
	
	@Test
	public void findDestinationAddress() {
		IPPacket pkg = new IPPacket();
		pkg.setVersion((byte) 1);
		pkg.setDifferentiatedServices((byte) 6);
		pkg.setExplicitCongestionNotification((byte) 2);
		pkg.setTotalLength(5000);
		pkg.setIdentification(23);
		pkg.setFlags((byte) 2);
		pkg.setFragmentOffset(5);
		pkg.setTimeToLive((byte) 5);
		pkg.setProtocol((byte) 4);
		pkg.setHeaderChecksum(12345);
		pkg.setSourceIPAddress(234532);
		pkg.setDestIPAddress(NetUtils.wrap("10.0.0.1").toInt());
		pkg.setData("This is some data".getBytes());
		
		//13, 14, 15, 16
		
	}
}
