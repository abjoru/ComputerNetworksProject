package edu.fit.cs.computernetworks.utils;

import static java.lang.String.format;

public class SimpleLogger {
	
	private final String loggerName;
	
	public SimpleLogger(final String name) {
		this.loggerName = name;
	}
	
	public void log(final String message) {
		System.out.printf("[%s] %s%n", loggerName, message);
	}
	
	public void log(final String pattern, final Object...args) {
		log(format(pattern, args));
	}
	
	public void error(final String message) {
		System.err.printf("[%s] %s%n", loggerName, message);
	}

}
