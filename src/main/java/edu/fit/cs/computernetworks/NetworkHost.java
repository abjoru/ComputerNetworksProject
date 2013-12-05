package edu.fit.cs.computernetworks;

import static java.lang.String.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import edu.fit.cs.computernetworks.model.Address;
import edu.fit.cs.computernetworks.model.EthernetFrame;
import edu.fit.cs.computernetworks.model.IPPacket;
import edu.fit.cs.computernetworks.model.TCPSegment;
import edu.fit.cs.computernetworks.topology.Host;
import edu.fit.cs.computernetworks.topology.Topology;
import edu.fit.cs.computernetworks.utils.IP;
import edu.fit.cs.computernetworks.utils.NetUtils;

public class NetworkHost extends AbstractNetworkNode<Host> implements Runnable {
	final Logger logger;
	
	private final Map<IP, List<TCPSegment>> receiveBuffer;

	private int receiveIndex = 0;

	public NetworkHost(final Topology topology, final Host descriptor) {
		super(topology, descriptor);
		this.logger = Logger.getLogger(descriptor.id);
		this.receiveBuffer = new HashMap<>();
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
				logger.info(format("Wrote file '%s'", filename));
			}
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void transport(final byte[] payload, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND: {
			// Check: Make sure segments are created for the given byte array based on MTU
			
			if(payload.length > getLocalMTU(null)) {
				// Local counter
				int iCount = 0;
				
				final ByteBuffer bBuff = ByteBuffer.wrap(payload);
				// Get all the header size
				final int segSize = getLocalMTU(null) - TCPSegment.HEADER_SIZE - IPPacket.HEADER_SIZE - EthernetFrame.HEADER_LENGTH;
				// Check: Make sure there is still bytes in the payload
				while(bBuff.hasRemaining()) {
					final byte[] segSlice = new byte[Math.min(bBuff.remaining(), segSize)];
					bBuff.get(segSlice);
					
					// Check: which flag is set
					final byte bFlag = bBuff.hasRemaining() ? TCPSegment.SYN : TCPSegment.FIN;
					
					// Construct TCP segment and set payload
					final TCPSegment seg = new TCPSegment(iCount++, bFlag, addr.getSourcePort(), addr.getDestPort());
					seg.setPayload(segSlice);

					// Deliver to link-layer
					logger.info("Sending");
					networkLayer(seg.toByteArray(), transmit, addr);	
				}
			}
			else {
				// Setup the TCP header
				// Construct TCP segment and set payload
				final TCPSegment seg = new TCPSegment(0, TCPSegment.FIN, addr.getSourcePort(), addr.getDestPort());
				seg.setPayload(payload);

				// Deliver to link-layer
				logger.info("Sending");
				networkLayer(seg.toByteArray(), transmit, addr);
			}
			
			break;
		}
		case RECEIVE: {
			logger.info("Received");
			
			// Reconstruct TCP segment and validate header
			final TCPSegment seg = TCPSegment.from(payload);
			final int checksum = seg.getHeaderChecksum();
			if (!seg.validate(checksum)) {
				logger.severe("TCP header checksum mismatch! Dropping package...");
				return;
			}
			
			// Update the list with the segments
			logger.info("Adding Segment to buffer");
			final IP key = addr.getSourceAddress();
			bufferFor(key).add(seg.getSeqNum(), seg);
			
			// Check: Make sure we have only one segment or many of them
			if(seg.isFin()) {
				logger.info("Received last segment");
				int size = 0;
				
				final List<TCPSegment> segments = receiveBuffer.remove(key);
				
				// Construct the buffer
				for(final TCPSegment s : segments) {
					size += s.getPayload().length;
				}
				
				// Allocate the buffer
				final ByteBuffer newPayload = ByteBuffer.allocate(size);
				for(final TCPSegment s : segments) {
					newPayload.put(s.getPayload());
				}
								
				// Application layer
				logger.info(format("Assembling %d segments", segments.size()));
				application(newPayload.array(), addr);
			}
						
			break;
		}
		default: // no-op
		}
	}

	private List<TCPSegment> bufferFor(IP source) {
		synchronized (receiveBuffer) {
			if (!receiveBuffer.containsKey(source)) {
				receiveBuffer.put(source, new ArrayList<TCPSegment>());
			}
			
			return receiveBuffer.get(source);
		}
	}

	@Override
	public void run() {
		logger.info(format("Starting with observable directory: '%s'", descriptor.observable().getAbsolutePath()));
		while (true) {
			final String[] files = descriptor.observable().list(
					new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							return name.split("\\.")[0].length() == 1;
						}
					});

			if (files != null && files.length > 0) {
				logger.info("New files discovered");
				for (final String name : files) {
					final File f = new File(descriptor.observable(), name);
					final String targetHostname = name.split("\\.")[0];
					final Host resolved = (Host) topology.resolve(targetHostname);

					if (resolved == null) {
						logger.severe("Unable to resolve host! Deleting file...");
						f.delete();
						continue;
					}

					try {
						final byte[] data = FileUtils.readFileToByteArray(f);

						f.delete();
						logger.info(format("%s read, sending to transport layer...", f.getName()));
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
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#networkAddress(edu.fit.cs.computernetworks.utils.IP)
	 */
	@Override
	public IP networkAddress(IP ifAddress) {
		return descriptor.toNetworkAddress();
	}
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#matchesInterface(byte[])
	 */
	@Override
	public boolean matchesInterface(byte[] macAddr) {
		return Arrays.equals(macAddr, getLocalMAC(null));
	}
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#belongsToNetwork(edu.fit.cs.computernetworks.utils.IP)
	 */
	@Override
	public boolean belongsToNetwork(IP networkAddr) {
		return descriptor.matchesNetwork(networkAddr);
	}

	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#getLocalMTU(edu.fit.cs.computernetworks.utils.IP)
	 */
	@Override
	public int getLocalMTU(final IP localIp) {
		return descriptor.mtu;
	}
	
	/* (non-Javadoc)
	 * @see edu.fit.cs.computernetworks.AbstractNetworkNode#getLocalMAC(edu.fit.cs.computernetworks.utils.IP)
	 */
	@Override
	public byte[] getLocalMAC(final IP localIp) {
		return NetUtils.macToByteArray(descriptor.mac);
	}

}
