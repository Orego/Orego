package edu.lclark.orego.experiment;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Some convenience methods for logging. */
public final class Logging {

	/** The logger to use, or null if logging is not turned on. */
	private static Logger logger = null;

	private static String previousTimeStamp = null;

	/**
	 * Logs a message at the specified level.
	 *
	 * @see Logger#log(Level, String)
	 */
	public static synchronized void log(Level level, String message) {
		if (logger != null) {
			String stamp = GameBatch.timeStamp(false);
			logger.log(level, stamp + " thread "
					+ Thread.currentThread().getId() + " " + message);
			// NOTE: This won't catch long delays that happen to include
			// midnight
			if (previousTimeStamp != null && rawTime(stamp) - rawTime(previousTimeStamp) > 5) {
				logger.log(level, stamp + " thread "
						+ Thread.currentThread().getId() + " " + "LONG DELAY!");
			}
			previousTimeStamp = stamp;
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
	 * Returns the time represented by stamp as a number of minutes since the
	 * beginning of the day.
	 */
	public static int rawTime(String stamp) {
		int hour = Integer.parseInt(stamp.substring(11, 13));
		int minute = Integer.parseInt(stamp.substring(14, 16));
		return 60 * hour + minute;
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

}
