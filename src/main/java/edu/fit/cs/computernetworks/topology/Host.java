package edu.fit.cs.computernetworks.topology;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.fit.cs.computernetworks.utils.IP;
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
	public IP nextHopTo(final IP destIP) {
		final RoutingEntry directRoute = getDirectRoute(destIP);
		if (directRoute == null) {
			final IP netAddr = NetUtils.networkAddress(destIP, NetUtils.wrap(mask));
			for (final RoutingEntry e : routing) {
				final IP checkAddr = e.networkAddress();
				if (netAddr.equals(checkAddr)) {
					return NetUtils.wrap(e.nextHop);
				}
			}
		} else {
			return NetUtils.wrap(directRoute.nextHop);
		}
		
		return null;
	}
	
	private RoutingEntry getDirectRoute(final IP destIp) {
		for (final RoutingEntry e : routing) {
			final IP network = NetUtils.wrap(e.network);
			if (network.equals(destIp)) {
				return e;
			}
		}
		
		return null;
	}
}
