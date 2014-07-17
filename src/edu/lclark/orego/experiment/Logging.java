package edu.lclark.orego.experiment;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {

	private static String logFilePath;

	private static Logger logger = Logger.getLogger("orego-default");

	public static void setFilePath(String filePath) {
		logFilePath = filePath;
		try {
			FileHandler handler = new FileHandler(filePath);
			handler.setFormatter(new PlainTextFormatter());
			logger.addHandler(handler);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static String getFilePath() {
		return logFilePath;
	}

}
