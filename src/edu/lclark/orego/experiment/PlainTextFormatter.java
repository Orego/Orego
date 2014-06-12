package edu.lclark.orego.experiment;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class PlainTextFormatter extends Formatter{

	@Override
	public String format(LogRecord record) {
		return record.getMessage();
	}

}
