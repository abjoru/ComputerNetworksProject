package edu.fit.cs.computernetworks.model;


public interface OSIPacket {
	
	public byte[] getHeader();
	public byte[] getData();
	
	/**
	 * Complete packet including header and data
	 * 
	 * @return
	 */
	public byte[] toByteArray();

}
