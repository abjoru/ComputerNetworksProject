package edu.fit.cs.computernetworks.topology;

import java.io.File;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.fit.cs.computernetworks.utils.NetUtils;

public class Host extends Node {
	static final String PATH_SEPARATOR = System.getProperty("file.separator");
	
	public String ip;
	public String mask;
	public String mac;
	public int mtu;
	public String gateway;
	
	@JsonIgnore
	private File observableDir;
	
	@JsonIgnore
	/*package*/ String rootPath = PATH_SEPARATOR + "tmp";
	
	public File observable() {
		if (observableDir == null) {
			observableDir = new File(rootPath + PATH_SEPARATOR + id);
		}
		
		return observableDir;
	}
	
	@Override
	public byte[] nextHopTo(byte[] destIP) {
		final byte[] netAddr = NetUtils.networkAddress(destIP, NetUtils.ipToByteArray(mask));
		for (final RoutingEntry e : routing) {
			if (Arrays.equals(netAddr, e.networkAddress())) {
				return NetUtils.ipToByteArray(e.nextHop);
			}
		}
		
		return null;
	}
}
