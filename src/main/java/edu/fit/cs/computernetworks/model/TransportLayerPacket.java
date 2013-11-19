package edu.fit.cs.computernetworks.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.fit.cs.computernetworks.topology.Node;
import edu.fit.cs.computernetworks.topology.Port;

public class TransportLayerPacket implements OSILayerPacket {
	
	/**
	 * Construct transport layer packages from the given input. This method
	 * should segment the packages based on the node MTU.
	 * 
	 * @param msg
	 * @param addr
	 * @param ports
	 * @return
	 */
	public static Collection<TransportLayerPacket> from(final byte[] msg, final Address addr, final List<Port> ports) {
		return Collections.emptyList();
	}

	@Override
	public byte[] toByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public Address getDestinationAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getSegmentId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean matchesAddress(Node self) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getData() {
		// TODO Auto-generated method stub
		return null;
	}

}
