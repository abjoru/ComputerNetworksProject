package edu.fit.cs.computernetworks.topology;

import java.util.List;

public class Node {

	private String id;
	private String type;
	private List<Port> ports;
	private List<String> links;
	
	public String getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
	
	public List<Port> getPorts() {
		return ports;
	}
	
	public List<String> getLinks() {
		return links;
	}
}
