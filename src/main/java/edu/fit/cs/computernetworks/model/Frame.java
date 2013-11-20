package edu.fit.cs.computernetworks.model;

import java.nio.ByteBuffer;

public class Frame {
	public static final int HEADER_LENGTH = 8;
	
	private byte startFlag = 0b01111110;
	private byte address = 0b00000000;
	private byte control = 0b00000000;
	private short protocol = 0b0000000000100001; // IP
	private byte[] payload;
	private short checksum = 0b0000000000000000;
	private byte endFlag = 0b01111110;
	
	public byte[] getPayload() {
		return payload;
	}
	
	public void setPayload(final byte[] data) {
		this.payload = data;
		// TODO calculate checksum
	}
	
	public static Frame from(final byte[] data) {
		final ByteBuffer buffer = ByteBuffer.wrap(data);
		final Frame frame = new Frame();
		
		frame.startFlag = buffer.get();
		frame.address = buffer.get();
		frame.control = buffer.get();
		frame.protocol = buffer.getShort();
		
		// get payload based on data length and location
		frame.payload = new byte[data.length - 8];
		buffer.get(frame.payload);
		
		frame.checksum = buffer.getShort();
		frame.endFlag = buffer.get();
		
		return frame;
	}
	
	public byte[] toByteArray() {
		// might actually be less, but good for now..
		final ByteBuffer buffer = ByteBuffer.allocate(payload.length + 8);
		
		buffer.put(startFlag); // start
		buffer.put(address); // address
		buffer.put(control); // control
		buffer.putShort(protocol);
		buffer.put(payload); // payload
		buffer.putShort(checksum);
		buffer.put(endFlag); // end
		
		return buffer.array();
	}

}
