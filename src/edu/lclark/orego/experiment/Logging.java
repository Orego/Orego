package edu.lclark.orego.experiment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Some convenience methods for logging. */
public final class Logging {

	private static final Calendar CALENDAR = Calendar.getInstance();

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/** The logger to use, or null if logging is not turned on. */
	private static Logger logger = null;

	/**
	 * Logs a message at the specified level.
	 *
	 * @see Logger#log(Level, String)
	 */
	public static void log(Level level, String message) {
		if (logger != null) {
			logger.log(level, message);
		}
	}

	/**
	 * Logs a message at the default INFO level.
	 *
	 * @see Logger#log(Level, String)
	 */
	public static void log(String message) {
		log(Level.INFO, message);
	}
	
	/**
	 * Sets logging to appear in a timestamped file in directory. Behavior is
	 * undefined if two instances of Orego are launched at the same millisecond.
	 */
	public static void setFilePath(String directory) {
		logger = Logger.getLogger("orego-default");
		new File(directory).mkdir();
		directory += File.separator + GameBatch.timeStamp(false) + ".log";
		try {
			final FileHandler handler = new FileHandler(directory);
			handler.setFormatter(new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMessage() + "\n";
				}
			});
			logger.addHandler(handler);
			logger.setLevel(Level.ALL);
			logger.setUseParentHandlers(false);
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/** Returns a human-readable String representing the current time. */
	public static String timeStamp() {
		return DATE_FORMAT.format(CALENDAR.getTime());
	}

}
