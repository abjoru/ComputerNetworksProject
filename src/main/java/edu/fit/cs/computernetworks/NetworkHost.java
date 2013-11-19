package edu.fit.cs.computernetworks;

import static java.lang.String.format;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.TransportLayer;
import edu.fit.cs.computernetworks.topology.Host;
import edu.fit.cs.computernetworks.topology.Topology;

public class NetworkHost extends NetworkNode<Host> implements Runnable {
	
	private final File observable;
	private final TransportLayer transportLayer = new TransportLayer();
	
	public NetworkHost(final Topology topology, final Host descriptor, final File dir, final NodeManager mgr) {
		super(topology, descriptor, mgr);
		this.observable = dir;
	}
	
	private void application(byte[] assemble, Address addr) {
	}
	/*
	public void transport(final byte[] msg, final Transmit type, final Address addr) {
		switch (type) {
		case SEND: // I.e. this node sends data to some destination..
			for (final OSILayerPacket pkg : TransportLayerPacket.from(msg, addr, descriptor.getPorts())) {
				networkLayer(pkg.toByteArray(), Transmit.SEND, addr);
			}
			break;
		case RECEIVE: // I.e. received transmission from lower levels..
			// Effectively transform input to TransportLayerPacket
			
			TransportLayerPacket segment = transportLayer.handleReceive(msg);
			if (segment.matchesAddress(descriptor)) {
				final long sid = segment.getSegmentId();
				if (sid > 0) { // does no segments represent -1?
					if (transportLayer.queue(segment)) { // return true if all segments received?
						application(transportLayer.assemble(segment), null);
					}
				}
			}
			break;
		default: // no-op
		}
	}
*/
	@Override
	public void networkLayer(final byte[] msg, final Transmit transmit, final Address addr) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void run() {
		while (true) {
			final String[] files = observable.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(format("%s-", descriptor.id));
				}
			});
			
			if (files != null && files.length > 0) {
				for (final String name : files) {
					final File f = new File(observable, name);
					final String localIp = ((Host) descriptor).ip;
					final String destIp = manager.hostByName(name.split("-")[1]).ip;
					final Address addr = new Address(localIp, destIp);
					
					try {
						final byte[] data = FileUtils.readFileToByteArray(f);
						application(data, addr);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				try {
					// TODO make configurable
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}		
	}

}
