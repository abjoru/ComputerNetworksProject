package edu.fit.cs.computernetworks.model;

import org.junit.Assert;
import org.junit.Test;

import edu.fit.cs.computernetworks.utils.NetUtils;

public class EthernetFrameTest {
	
	@Test
	public void toFromByteArray() {
		String payload = "This is the payload";
		byte[] srcMac = NetUtils.macToByteArray("00:B0:D0:86:BB:F7");
		byte[] destMac = NetUtils.macToByteArray("00:B0:D0:86:F7:BB");
		
		EthernetFrame frame = new EthernetFrame(srcMac, destMac);
		frame.setPayload(payload.getBytes());
		
		byte[] data = frame.toByteArray();
		Assert.assertNotNull(data);
		
		EthernetFrame rebuilt = EthernetFrame.from(data);
		Assert.assertNotNull(rebuilt);
		Assert.assertNotNull(rebuilt.getPayload());
		Assert.assertEquals(payload, new String(rebuilt.getPayload()));
	}
	
	@Test
	public void crcCheck() {
		String payload = "This is the payload";
		byte[] srcMac = NetUtils.macToByteArray("00:B0:D0:86:BB:F7");
		byte[] destMac = NetUtils.macToByteArray("00:B0:D0:86:F7:BB");
		
		EthernetFrame frame = new EthernetFrame(srcMac, destMac);
		frame.setPayload(payload.getBytes());
		
		int crc = frame.getCrc32();
		int corruptedCrc = crc - 5;
		
		byte[] data = frame.toByteArray();
		EthernetFrame f = EthernetFrame.from(data);
		
		Assert.assertTrue(f.validate(crc));
		Assert.assertFalse(f.validate(corruptedCrc));

		// Corrupt first byte
		data[0] = (byte) 0b11100010;
		f = EthernetFrame.from(data);
		Assert.assertFalse(f.validate(crc));
	}

}
