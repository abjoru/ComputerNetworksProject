package edu.fit.cs.computernetworks.topology;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Host extends Node {
	public String ip;
	public String mask;
	public String mac;
	public int mtu;
	public String gateway;
	public String folder;
	
	@JsonIgnore
	private File observableDir;
	
	public File observable() {
		if (observableDir == null) {
			observableDir = new File(folder);
		}
		
		return observableDir;
	}
}
