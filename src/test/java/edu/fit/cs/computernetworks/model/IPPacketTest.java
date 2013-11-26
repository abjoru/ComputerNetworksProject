package edu.fit.cs.computernetworks.model;

import org.junit.Assert;
import org.junit.Test;

import edu.fit.cs.computernetworks.utils.IP;

public class IPPacketTest {
	
	@Test
	public void toFromByteArray() {
		IPPacket pkg = new IPPacket();
		pkg.setIdentification(23);
		pkg.setFlags((byte) 2);
		pkg.setFragmentOffset(5);
		pkg.setTimeToLive((byte) 5);
		pkg.setSourceIPAddress(234532);
		pkg.setDestIPAddress(546532);
		pkg.setPayload("This is some data".getBytes());
		
		byte[] bytes = pkg.toByteArray();
		Assert.assertNotNull(bytes);
		
		IPPacket spawned = IPPacket.from(bytes);
		Assert.assertNotNull(spawned);
		Assert.assertEquals(4, spawned.getVersion());
		Assert.assertEquals(23, spawned.getIdentification());
		Assert.assertEquals(2, spawned.getFlags());
		Assert.assertEquals(5, spawned.getFragmentOffset());
		Assert.assertEquals(5, spawned.getTimeToLive());
		Assert.assertEquals(234532, spawned.getSourceIPAddress());
		Assert.assertEquals(546532, spawned.getDestIPAddress());
		Assert.assertArrayEquals("This is some data".getBytes(), spawned.getPayload());
	}

	@Test
	public void checksum() {
		String payload = "This is a payload";
		IP source = new IP("10.0.0.1");
		IP dest = new IP("10.0.0.2");
		IPPacket pkg = new IPPacket(0, source.toInt(), dest.toInt());
		pkg.setPayload(payload.getBytes());
		
		int checksum = pkg.getHeaderChecksum();
		int corrupted = checksum - 10;
		
		byte[] data = pkg.toByteArray();
		IPPacket p = IPPacket.from(data);
		
		Assert.assertTrue(p.validate(checksum));
		Assert.assertFalse(p.validate(corrupted));
		
		// Corrupt first byte
		data[0] = (byte) 0b11100010;
		p = IPPacket.from(data);
		Assert.assertFalse(p.validate(checksum));
	}
}
