package edu.lclark.orego.experiment;

import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;

public final class Logging {

	private static String logFilePath;

	private static Logger logger = null;

	public static void setFilePath() {
		logger = Logger.getLogger("orego-default");
		logFilePath = SYSTEM.resultsDirectory;
		logFilePath += "logs" + GameBatch.timeStamp(false) + ".log";
		try {
			FileHandler handler = new FileHandler(logFilePath);
			handler.setFormatter(new PlanTextFormatter());
			logger.addHandler(handler);
			logger.setLevel(Level.ALL);
			logger.setUseParentHandlers(false);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static String getFilePath() {
		return logFilePath;
	}
	
	public static Logger getLogger(){
		return logger;
	}
	
	public static void log(String message){
		log(Level.INFO, message);
	}
	
	public static void log(Level level, String message){
		if (logger != null) {
			logger.log(level, message);
		}
	}
}

class PlanTextFormatter extends Formatter{

	@Override
	public String format(LogRecord record) {
		return record.getMessage() + "\n";
	}
	
}