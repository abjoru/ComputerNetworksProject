package edu.fit.cs.computernetworks.model;

import org.junit.Assert;
import org.junit.Test;

public class TCPSegmentTest {
	
	@Test
	public void toFromByteArray() {
		TCPSegment seg = new TCPSegment(0, TCPSegment.FIN, (short) 1111, (short) 1111);
		seg.setPayload("This is the payload".getBytes());
		
		byte[] bytes = seg.toByteArray();
		Assert.assertNotNull(bytes);
		
		TCPSegment s = TCPSegment.from(bytes);
		Assert.assertNotNull(s);
		Assert.assertEquals(0, s.getSeqNum());
		Assert.assertTrue(s.isFin());
		Assert.assertEquals((short) 1111, s.getSourcePort());
		Assert.assertEquals((short) 1111, s.getDestPort());
		Assert.assertArrayEquals("This is the payload".getBytes(), s.getPayload());
	}
	
	@Test
	public void checksum() {
		String payload = "This is a payload";
		TCPSegment seg = new TCPSegment(0, TCPSegment.FIN, (short) 1111, (short) 1111);
		seg.setPayload(payload.getBytes());
		
		int checksum = seg.getHeaderChecksum();
		int corrupted = checksum - 10;
		
		byte[] data = seg.toByteArray();
		TCPSegment s = TCPSegment.from(data);
		
		Assert.assertTrue(s.validate(checksum));
		Assert.assertFalse(s.validate(corrupted));
	}

}
