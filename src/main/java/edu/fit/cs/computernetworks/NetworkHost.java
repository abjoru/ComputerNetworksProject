package edu.fit.cs.computernetworks;

import static java.lang.String.format;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.topology.Host;
import edu.fit.cs.computernetworks.topology.Topology;

public class NetworkHost extends AbstractNetworkNode<Host> implements Runnable {
	
	public NetworkHost(final Topology topology, final Host descriptor) {
		super(topology, descriptor);
	}
	
	private void application(byte[] assemble, Address addr) {
	}

	public void transport(final byte[] payload, final Transmit transmit, final Address addr) {
		
	}
	
	@Override
	public void run() {
		while (true) {
			final String[] files = descriptor.observable().list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(format("%s-", descriptor.id));
				}
			});
			
			if (files != null && files.length > 0) {
				for (final String name : files) {
					final File f = new File(descriptor.observable(), name);
					final String targetHostname = name.split("-")[0];
					final Host resolved = (Host) topology.resolve(targetHostname);
					
					if (resolved == null) {
						// TODO logger no such host
						f.delete();
						continue;
					}
					
					try {
						final byte[] data = FileUtils.readFileToByteArray(f);
						application(data, new Address(descriptor.ip, resolved.ip));
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

	@Override
	public int mtu(final String localIp) {
		return descriptor.mtu;
	}

}
