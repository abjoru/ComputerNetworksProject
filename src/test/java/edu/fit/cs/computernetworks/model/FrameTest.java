package edu.fit.cs.computernetworks.model;

import org.junit.Assert;
import org.junit.Test;

public class FrameTest {
	
	@Test
	public void toFromByteArray() {
		String payload = "This is the payload";
		
		Frame frame = new Frame();
		frame.setPayload(payload.getBytes());
		
		byte[] data = frame.toByteArray();
		Assert.assertNotNull(data);
		
		Frame rebuilt = Frame.from(data);
		Assert.assertNotNull(rebuilt);
		Assert.assertNotNull(rebuilt.getPayload());
		Assert.assertEquals(payload, new String(rebuilt.getPayload()));
	}

}
