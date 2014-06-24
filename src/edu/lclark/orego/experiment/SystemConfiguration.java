package edu.lclark.orego.experiment;

import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import edu.lclark.orego.ui.Orego;

import java.util.*;

/** Holds system-dependent properties, e.g., classpath. */
enum SystemConfiguration {

	/** Name of the singleton instance. */
	SYSTEM;
	
	/** Command to run Java, including Orego classpath. */
	final String javaWithOregoClasspath;

	/** Command to run GNUGo. */
	final String gnugoHome;
	
	/** List of hosts on which to run experiments. */
	final List<String> hosts;
	
	/** Directory in which to store result files. */
	final String resultsDirectory;
	
	/** Reads settings from config/system.properties. */
	SystemConfiguration() {
		Properties properties = new Properties();
		final String oregoRoot = Orego.class
				.getProtectionDomain().getCodeSource().getLocation().getFile()
				+ ".." + File.separator;
		try {
			properties.load(new FileInputStream(oregoRoot
					+ separator + "config" + separator + "system.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		javaWithOregoClasspath = properties
				.getProperty("javaWithOregoClassPath");
		gnugoHome = properties
				.getProperty("gnugoHome");
		resultsDirectory = properties.getProperty("resultsDirectory");
		hosts = new ArrayList<>();
		for (String s : properties.stringPropertyNames()) {
			if (s.startsWith("host")) {
				hosts.add((String) properties.get(s));
			}
		}
	}

}
