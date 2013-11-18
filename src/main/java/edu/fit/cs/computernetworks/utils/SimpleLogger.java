package edu.fit.cs.computernetworks.utils;

public class SimpleLogger {
	
	private final String loggerName;
	
	public SimpleLogger(final String name) {
		this.loggerName = name;
	}
	
	public void log(String message) {
		System.out.printf("[%s] %s%n", loggerName, message);
	}
	
	public void error(String message) {
		System.err.printf("[%s] %s%n", loggerName, message);
	}

}
