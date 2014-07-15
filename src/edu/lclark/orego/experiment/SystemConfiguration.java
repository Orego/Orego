package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static java.io.File.separator;
import static java.lang.Integer.parseInt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(OREGO_ROOT + separator
					+ "config" + separator + "system.properties"));
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		oregoClassPath = properties.getProperty("oregoClassPath");
		megabytes = parseInt(properties.getProperty("megabytes"));
		gnugoHome = properties.getProperty("gnugoHome");
		java = properties.getProperty("java");
		resultsDirectory = properties.getProperty("resultsDirectory");
		hosts = new ArrayList<>();
		for (final String s : properties.stringPropertyNames()) {
			if (s.startsWith("host")) {
				hosts.add((String) properties.get(s));
			}
		}
	}

}
