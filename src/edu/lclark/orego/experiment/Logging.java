package edu.lclark.orego.experiment;

public class Logging {
	
	private static String logFilePath;
	
	public static void setFilePath(String filePath){
		logFilePath = filePath;
	}
	
	public static String getFilePath(){
		return logFilePath;
	}

}
