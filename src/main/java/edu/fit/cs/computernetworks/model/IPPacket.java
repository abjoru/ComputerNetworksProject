package edu.fit.cs.computernetworks.model;

import static edu.fit.cs.computernetworks.utils.ErrorCheckUtils.checksum;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

public class IPPacket {
	public static final int HEADER_SIZE = 20; // in bytes

	private byte version = 4; // 1 nibble
	private byte internetHeaderLength = HEADER_SIZE * 8 / 32; // 1 nibble
	private byte differentiatedServices = 0; // 6 bits
	private byte explicitCongestionNotification = 0; // 2 bits
	private int totalLength = HEADER_SIZE; // 2 bytes
	private int identification; // 2 bytes
	private byte flags = 0; // 3 bits
	private int fragmentOffset = 0; // 13 bits
	private byte timeToLive = 0; // 1 byte
	private byte protocol = 6; // 1 byte (6 = TCP)
	private int headerChecksum = 0; // 2 bytes
	private int sourceIPAddress; // 4 bytes
	private int destIPAddress; // 4 bytes
	// private int options; // variable, not implemented!
	
	private byte[] payload;
	
	IPPacket() {
		// package protected default constructor
	}
	
	public IPPacket(final int id, final int srcAddr, final int destAddr) {
		this.identification = id;
		this.sourceIPAddress = srcAddr;
		this.destIPAddress = destAddr;
		this.headerChecksum = checksum(getHeader());
	}
	
	public byte getVersion() {
		return version;
	}
	
	public int getIdentification() {
		return identification;
	}
	
	public void setIdentification(int identification) {
		this.identification = identification;
	}
	
	public byte getFlags() {
		return flags;
	}
	
	public void setFlags(byte flags) {
		this.flags = flags;
	}
	
	public int getFragmentOffset() {
		return fragmentOffset;
	}
	
	public void setFragmentOffset(int fragmentOffset) {
		this.fragmentOffset = fragmentOffset;
	}
	
	public byte getTimeToLive() {
		return timeToLive;
	}
	
	public void setTimeToLive(byte timeToLive) {
		this.timeToLive = timeToLive;
	}
	
	public int getHeaderChecksum() {
		return headerChecksum;
	}
	
	public int getSourceIPAddress() {
		return sourceIPAddress;
	}
	
	public void setSourceIPAddress(int sourceIPAddress) {
		this.sourceIPAddress = sourceIPAddress;
	}
	
	public int getDestIPAddress() {
		return destIPAddress;
	}
	
	public void setDestIPAddress(int destIPAddress) {
		this.destIPAddress = destIPAddress;
	}

	public byte[] getPayload() {
		return payload;
	}
	
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	public boolean validate(final int checksum) {
		this.headerChecksum = 0;
		final int check = checksum(getHeader());
		return check == checksum;
	}
	
	public byte[] getHeader() {
		final ByteBuffer buff = ByteBuffer.allocate(HEADER_SIZE);
		
		if (totalLength == 0) {
			totalLength = HEADER_SIZE + payload.length;
		}
		
		buff.put((byte) ((version << 4) | internetHeaderLength));
		buff.put((byte) ((differentiatedServices << 2) | explicitCongestionNotification));
		buff.put((byte) ((totalLength << 16) >> 24));
		buff.put((byte) ((totalLength << 24) >> 24));
		buff.put((byte) ((identification << 16) >> 24));
		buff.put((byte) ((identification << 24) >> 24));
		buff.put((byte) ((flags << 5) | (byte) ((fragmentOffset << 16) >> 24)));
		buff.put((byte) ((fragmentOffset << 24) >> 24));
		buff.put(timeToLive);
		buff.put(protocol);
		buff.put((byte) ((headerChecksum << 16) >> 24));
		buff.put((byte) ((headerChecksum << 24) >> 24));
		buff.put((byte) ((sourceIPAddress >> 24)));
		buff.put((byte) ((sourceIPAddress << 8) >> 24));
		buff.put((byte) ((sourceIPAddress << 16) >> 24));
		buff.put((byte) ((sourceIPAddress << 24) >> 24));
		buff.put((byte) ((destIPAddress >> 24)));
		buff.put((byte) ((destIPAddress << 8) >> 24));
		buff.put((byte) ((destIPAddress << 16) >> 24));
		buff.put((byte) ((destIPAddress << 24) >> 24));

		return buff.array();
	}

	public static IPPacket from(final byte[] msg) {
		final IPPacket pkg = new IPPacket();
		final ByteBuffer buff = ByteBuffer.wrap(msg);

		byte versionAndHeaderLength = buff.get();
		pkg.version = (byte) ((versionAndHeaderLength >> 4) & 0x0f);
		pkg.internetHeaderLength = (byte) (versionAndHeaderLength & 0x0f);

		byte diffAndEcn = buff.get();
		pkg.differentiatedServices = (byte) ((diffAndEcn >> 2) & 0b00111111);
		pkg.explicitCongestionNotification = (byte) ((diffAndEcn & 0b00000011));

		pkg.totalLength = ByteBuffer.wrap(
				new byte[] { 0, 0, buff.get(), buff.get() }).getInt();
		pkg.identification = ByteBuffer.wrap(
				new byte[] { 0, 0, buff.get(), buff.get() }).getInt();

		byte temp = buff.get();
		pkg.flags = (byte) ((temp >> 5) & 0b00000111);
		pkg.fragmentOffset = ByteBuffer.wrap(
				new byte[] { 0, 0, (byte) (temp & 0b00011111), buff.get() })
				.getInt();

		pkg.timeToLive = buff.get();
		pkg.protocol = buff.get();

		pkg.headerChecksum = ByteBuffer.wrap(
				new byte[] { 0, 0, buff.get(), buff.get() }).getInt();
		pkg.sourceIPAddress = buff.getInt();
		pkg.destIPAddress = buff.getInt();
		
		pkg.payload = new byte[buff.remaining()];
		buff.get(pkg.payload);

		return pkg;
	}

	public byte[] toByteArray() {
		final byte[] header = getHeader();
		final byte[] data = getPayload();
		
		return ArrayUtils.addAll(header, data);
	}

}
