package edu.fit.cs.computernetworks.utils;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter {
	
	@Override
	public synchronized String format(LogRecord record) {
		return String.format("[%s] %s %s: %s%n", record.getLoggerName(),
				getClassName(record),
				record.getSourceMethodName(),
				formatMessage(record));
	}
	
	private String getClassName(final LogRecord record) {
		final String name = record.getSourceClassName();
		if (name == null) return "";
		
		return name.substring(name.lastIndexOf('.') + 1);
	}

}
