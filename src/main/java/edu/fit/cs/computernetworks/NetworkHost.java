package edu.fit.cs.computernetworks;

import static java.lang.Math.min;
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
	private final Logger logger;
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
	
	/**
	 * Transport Layer
	 * 
	 * <b>Sending payload</b>
	 * <p>When sending a given payload, it slices the payload into many segments based on the payload size. 
	 * First it will check the length of the payload to make sure it needs to be sliced to MTU's size or not.
	 * If the payload size is more than the MTU's size then it will slice the segments considering the header length.
	 * These segments has TCP header along with data. Source and destination ports are included in the header.
	 * SYN and FIN flags are used to identify segments starting and ending point.
	 * This payload will be sent to network layer for adding IP header and route this segment to right node.
	 * Sending parameters would be {@network #networkLayer(byte[], Transmit, Address)} method.</p>
	 * 
	 * <b>Receiving payloads</b>
	 * <p>When receiving a payload, this method will convert the payload into segments.
	 * It will then extract the chechsum to make sure segment is valid, if it's not valid it drops. 
	 * If not, it keeps accumulating the segments into a buffer. along with sequence number.
	 * Then it will check for the FIN flag in the header to confirm that that's the last segment.
	 * If it's not FIN (it must be SYN), then it keeps assembling the segments to build a complete payload.
	 * Once the payload is built, it will be sent to application layer for creating a file in destination node.</p>
	 * 
	 * @param payload
	 * @param transmit
	 * @param addr
	 * 
	 */
	@Override
	public void transport(final byte[] payload, final Transmit transmit, final Address addr) {
		switch (transmit) {
		case SEND: {
			// Check: Make sure segments are created for the given byte array based on MTU
			if(payload.length > getLocalMTU(null)) {
				// Local counter
				int segmentCount = 0;
				
				// Allocate buffer and calculate header size
				final ByteBuffer buffer = ByteBuffer.wrap(payload);
				final int segSize = getLocalMTU(null) - TCPSegment.HEADER_SIZE - IPPacket.HEADER_SIZE - EthernetFrame.HEADER_LENGTH;

				// Check: Make sure there is still bytes in the payload
				while(buffer.hasRemaining()) {
					final byte[] slice = new byte[min(buffer.remaining(), segSize)];
					buffer.get(slice);
					
					// Check: which flag is set
					final byte flag = buffer.hasRemaining() ? TCPSegment.SYN : TCPSegment.FIN;
					
					// Construct TCP segment and set payload
					final TCPSegment seg = new TCPSegment(segmentCount++, flag, addr.getSourcePort(), addr.getDestPort());
					seg.setPayload(slice);

					// Deliver to link-layer
					logger.info("Sending");
					networkLayer(seg.toByteArray(), transmit, addr);	
				}
			} else {
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
				
				// Count the payload length
				for(final TCPSegment s : segments) {
					size += s.getPayload().length;
				}
				
				// Allocate buffer and combine segments
				final ByteBuffer newPayload = ByteBuffer.allocate(size);
				for(final TCPSegment s : segments) {
					newPayload.put(s.getPayload());
				}
								
				// Deliver to application layer
				addr.setDestPort(seg.getDestPort());
				addr.setSourcePort(seg.getSourcePort());
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
