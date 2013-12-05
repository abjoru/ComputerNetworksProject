package edu.fit.cs.computernetworks.model;

import static edu.fit.cs.computernetworks.utils.ErrorCheckUtils.checksum;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

public class TCPSegment {
	
	public static final int HEADER_SIZE = 20; // in bytes
	public static final byte FIN = (byte) 0x01;
	public static final byte SYN = (byte) 0x02;

	private short sourcePort; 		// 2 bytes
	private short destPort; 			// 2 bytes
	private int seqNum;				// 4 bytes
	private int ackNum;				// 4 bytes
	private byte dataOffset = 0; 	// 1 nibble
	private byte reserveBits = HEADER_SIZE * 8 / 32; // 1 nibble
	private byte allFlags = 0;
	private short windowSize; 		// 2 bytes
	private short headerChecksum = 0; // 2 bytes
	private short urgentPtr; 			// 2 bytes
	private short totalLength = HEADER_SIZE; // 2 bytes
	
	// private int options; // variable, not implemented!
	
	private byte[] payload;
	
	TCPSegment() {
		// package protected default constructor
	}
	
	public TCPSegment(final int sNum, byte flag, final short srcPort, final short destPort) {
		this.seqNum = sNum;
		this.allFlags = flag;
		this.sourcePort = srcPort;
		this.destPort = destPort;
		this.headerChecksum = checksum(getHeader());
	}
	
	public int getHeaderChecksum() {
		return headerChecksum;
	}
	
	public boolean isFin() {
		return (allFlags == FIN); 
	}
	
	public boolean isSyn() {
		return (allFlags == SYN); 
	}
	
	public int getSourcePort() {
		return sourcePort;
	}
	
	public void setSourcePort(short sourcePort) {
		this.sourcePort = sourcePort;
	}
	
	public int getDestPort() {
		return destPort;
	}
	
	public void setDestPort(short destPort) {
		this.destPort = destPort;
	}

	public byte[] getPayload() {
		return payload;
	}
	
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	public boolean validate(final int checksum) {
		final short oldChecksum = this.headerChecksum;
		this.headerChecksum = 0;
		
		try {
			
			final int check = checksum(getHeader());
			return check == checksum;
		} finally {
			this.headerChecksum = oldChecksum;
		}
	}
	
	public int getSeqNum() {
		return seqNum;
	}
	
	public void setIdentification(int seqNum) {
		this.seqNum = seqNum;
	}
	
	public byte[] getHeader() {
		final ByteBuffer buff = ByteBuffer.allocate(HEADER_SIZE);
		
		if (totalLength == 0) {
			totalLength = (short) ((short) HEADER_SIZE + payload.length);
		}
		
		buff.putShort(sourcePort);
		buff.putShort(destPort);
		buff.putInt(seqNum);
		buff.putInt(ackNum);
		buff.put((byte) ((dataOffset << 4) | reserveBits));
		buff.put(allFlags);	
		buff.putShort(windowSize);
		buff.putShort(headerChecksum);
		buff.putShort(urgentPtr);
		
		return buff.array();
	}

	public static TCPSegment from(final byte[] msg) {
		final TCPSegment seg = new TCPSegment();
		final ByteBuffer buff = ByteBuffer.wrap(msg);

		seg.sourcePort = buff.getShort();
		seg.destPort = buff.getShort();
		
		seg.seqNum = buff.getInt();
		seg.ackNum = buff.getInt();

		byte offsetAndReserved = buff.get();
		seg.dataOffset = (byte) ((offsetAndReserved >> 4) & 0x0f);
		seg.reserveBits = (byte) (offsetAndReserved & 0x0f);

		seg.allFlags = buff.get();
		
		seg.windowSize = buff.getShort();
		seg.headerChecksum = buff.getShort();
		seg.urgentPtr = buff.getShort();
				
		seg.payload = new byte[buff.remaining()];
		buff.get(seg.payload);

		return seg;
	}

	public byte[] toByteArray() {
		final byte[] header = getHeader();
		final byte[] data = getPayload();
		
		return ArrayUtils.addAll(header, data);
	}

}
