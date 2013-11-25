package edu.fit.cs.computernetworks.model;

import static edu.fit.cs.computernetworks.utils.ErrorCheckUtils.crc32;

import java.nio.ByteBuffer;

public class EthernetFrame {
	public static final int HEADER_LENGTH = 30;
	
	private byte[] preamble;
	private byte startOfFrameDelimiter;
	private byte[] macDestination;
	private byte[] macSource;
	private int qTag;
	private short ethertype;
	private byte[] payload;
	
	private int crc;
	
	private EthernetFrame() {
		
	}
	
	public EthernetFrame(final byte[] macSource, final byte[] macDest) {
		this.preamble = new byte[] {
				(byte) 0b10101010, 
				(byte) 0b10101010,
				(byte) 0b10101010, 
				(byte) 0b10101010,
				(byte) 0b10101010,
				(byte) 0b10101010,
				(byte) 0b10101010
		};
		this.startOfFrameDelimiter = (byte) 0b10101011; // start of frame delim
		this.macSource = macSource;
		this.macDestination = macDest;
		this.qTag = 0;
		this.ethertype = 0;
	}
	
	public byte[] getPayload() {
		return payload;
	}
	
	public void setPayload(final byte[] data) {
		this.payload = data;
		this.crc = crc32(toByteArray());
	}

	public int getCrc32() {
		return crc;
	}
	
	public boolean validate(final int crc) {
		this.crc = 0;
		final int check = crc32(toByteArray());
		return crc == check;
	}
	
	public static EthernetFrame from(final byte[] data) {
		final ByteBuffer buffer = ByteBuffer.wrap(data);
		final EthernetFrame frame = new EthernetFrame();
		frame.macSource = new byte[6];
		frame.macDestination = new byte[6];
		
		frame.preamble = new byte[7];
		buffer.get(frame.preamble);
		
		frame.startOfFrameDelimiter = buffer.get();
		buffer.get(frame.macDestination);
		buffer.get(frame.macSource);
		frame.qTag = buffer.getInt();
		frame.ethertype = buffer.getShort();
		
		frame.payload = new byte[data.length - HEADER_LENGTH];
		buffer.get(frame.payload);
		frame.crc = buffer.getInt();
		
		return frame;
	}
	
	public byte[] toByteArray() {
		final ByteBuffer buffer = ByteBuffer.allocate(payload.length + HEADER_LENGTH);

		buffer.put(preamble);
		buffer.put(startOfFrameDelimiter);
		buffer.put(macDestination);
		buffer.put(macSource);
		buffer.putInt(qTag);
		buffer.putShort(ethertype);
		buffer.put(payload);
		buffer.putInt(crc);
		
		return buffer.array();
	}

}
