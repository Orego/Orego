package orego.experiment;

import java.io.*;
import orego.ui.Orego;

/**
 * This class provides static methods for sending debug messages to a graphic
 * window or to a file. (Such messages cannot be sent to stdout as it would
 * confuse GTP.)
 */
public class Debug {

	/** The file to which log messages are sent. */
	private static PrintWriter debugFile = null;
	
	/** The instance of the window. */
	private static boolean debugToStderr = false;

	/**
	 * The root directory for Orego. Code and data are in subdirectories of
	 * this.
	 */
	public static final String OREGO_ROOT_DIRECTORY = Orego.class
			.getProtectionDomain().getCodeSource().getLocation().getFile()
			+ ".." + File.separator;

	/**
	 * Prints s (and a newline) to the static instance of DebugFrame if the
	 * debug frame is enabled. Also prints s (and a newline) to debugFile if the
	 * debug file is enabled.
	 */
	public static void debug(Object s) {
		String text = s.toString();
		if (debugToStderr) {
			System.err.println(text);
		}
		if (debugFile != null) {
			debugFile.println(text);
			debugFile.flush();
		}
	}

	/**
	 * Specify debug logging to a file. If filename is null, turn off file logging.
	 * 
	 * @param filename full pathname to the file.
	 */
	public static void setDebugFile(String filename) {
		try {
			if (debugFile != null) {
				debugFile.close();
			}
			if (filename == null) {
				debugFile = null;
			} else {
				debugFile = new PrintWriter(filename);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** Indicate whether debugging messages are sent to stderr. */
	public static void setDebugToStderr(boolean value) {
		debugToStderr = value;
	}

}
