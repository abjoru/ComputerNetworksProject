package edu.fit.cs.computernetworks.model;

import java.io.Serializable;

public interface OSILayerPacket extends Serializable {
	
	public byte[] getHeader();
	public byte[] getData();
	
	/**
	 * Complete packet including header and data
	 * 
	 * @return
	 */
	public byte[] toByteArray();

}
