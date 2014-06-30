package edu.lclark.orego.experiment;

import static java.io.File.separator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import static java.lang.Integer.parseInt;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

/** Holds system-dependent properties, e.g., classpath. */
enum SystemConfiguration {

	/** Name of the singleton instance. */
	SYSTEM;

	/** Command to run GNUGo. */
	final String gnugoHome;

	/** List of hosts on which to run experiments. */
	final List<String> hosts;

	/** Command to run Java. */
	final String java;

	/** Megabytes of memory to allocate to Orego. */
	final int megabytes;

	/** Orego classpath. */
	final String oregoClassPath;
	
	/** Directory in which to store result files. */
	final String resultsDirectory;

	/** Reads settings from config/system.properties. */
	SystemConfiguration() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(OREGO_ROOT + separator
					+ "config" + separator + "system.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		oregoClassPath = properties.getProperty("oregoClassPath");
		megabytes = parseInt(properties.getProperty("megabytes"));
		gnugoHome = properties.getProperty("gnugoHome");
		java = properties.getProperty("java");
		resultsDirectory = properties.getProperty("resultsDirectory");
		hosts = new ArrayList<>();
		for (String s : properties.stringPropertyNames()) {
			if (s.startsWith("host")) {
				hosts.add((String) properties.get(s));
			}
		}
	}

}
