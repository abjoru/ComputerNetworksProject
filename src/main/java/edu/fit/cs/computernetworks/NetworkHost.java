package edu.fit.cs.computernetworks;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.commons.io.FileUtils;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.topology.Host;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class NetworkHost extends AbstractNetworkNode<Host> implements Runnable {
	private int receiveIndex = 0;
	
	public NetworkHost(final Topology topology, final Host descriptor) {
		super(topology, descriptor);
	}
	
	private void application(byte[] assemble, Address addr) {
		final File receivedDir = new File(descriptor.observable(), "received");
		if (!receivedDir.exists()) {
			receivedDir.mkdirs();
		}
		
		final ByteBuffer buffer = ByteBuffer.wrap(assemble);
		final String filename = format("file-%d.bin", receiveIndex++);
		final File outputFile = new File(receivedDir, filename);
		
		try {
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
			
			try (final FileOutputStream out = new FileOutputStream(outputFile)) {
				out.write(buffer.array());
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void transport(final byte[] payload, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND:
			// TODO implement me!
			networkLayer(payload, transmit, addr);
			break;
		case RECEIVE:
			application(payload, addr);
			break;
		default: // no-op
		}
	}
	
	@Override
	public void run() {
		logger.log(format("starting with observable directory: '%s'", descriptor.observable().getAbsolutePath()));
		while (true) {
			final String[] files = descriptor.observable().list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.split("\\.")[0].length() == 1;
				}
			});
			
			if (files != null && files.length > 0) {
				logger.log("new files discovered");
				for (final String name : files) {
					final File f = new File(descriptor.observable(), name);
					final String targetHostname = name.split("\\.")[0];
					final Host resolved = (Host) topology.resolve(targetHostname);
					
					if (resolved == null) {
						logger.error("Unable to resolve host! Deleting file...");
						f.delete();
						continue;
					}
					
					try {
						final byte[] data = FileUtils.readFileToByteArray(f);
						
						f.delete();
						logger.log("Data read, sending to transport layer...");
						transport(data, Transmit.SEND, new Address(descriptor.ip, resolved.ip));
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
	public int getLocalMTU(final IP localIp) {
		return descriptor.mtu;
	}
	
	@Override
	public byte[] getLocalMAC(final IP localIp) {
		return NetUtils.macToByteArray(descriptor.mac);
	}

}
