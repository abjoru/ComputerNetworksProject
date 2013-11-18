package edu.fit.cs.computernetworks.model;

public interface OSILayer<T extends OSILayerPacket> {
	
	// move more processing from node to this class
	// meaning that this method should return the final product of the layer
	public T handleReceive(final byte[] packet);

}
