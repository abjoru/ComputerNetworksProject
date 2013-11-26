package edu.fit.cs.computernetworks.topology;

import java.util.Arrays;
import java.util.BitSet;

import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class RoutingEntry {
	public String network;
	public String nextHop;
	
	public RoutingEntry() {
		// default constructor required by JSON mapper
	}
	
	public RoutingEntry(final String network, final String nextHop) {
		this.network = network;
		this.nextHop = nextHop;
	}

	/**
	 * Translates network address from string to byte array.
	 * This method also handles the case where the network
	 * address is given with a CIDR value <code>10.0.0/24</code>.
	 * 
	 * @return byte array representing the network address.
	 */
	public IP networkAddress() {
		final String[] segments = network.split("/");
		if (segments.length == 1) {
			return NetUtils.wrap(network);
		}

		final int cidr = Integer.valueOf(segments[1]);
		final String[] quadDec = segments[0].split("\\.");
		final BitSet bitMask = new BitSet(32);
		final byte[] netAddr = new byte[4];
		
		Arrays.fill(netAddr, (byte) 0);
		for (int i = 0; i < quadDec.length; i++) {
			netAddr[i] = (byte) Integer.parseInt(quadDec[i]);
		}
		
		for (int i = 0; i < cidr; i++) {
			bitMask.set(i);
		}
		
		 byte[] mask = bitMask.toByteArray();
		if (mask.length < 4) {
			final byte[] old = mask;
			mask = new byte[4];
			System.arraycopy(old, 0, mask, 0, old.length);
		}
		
		return NetUtils.networkAddress(NetUtils.wrap(netAddr), NetUtils.wrap(mask));
	}
}
