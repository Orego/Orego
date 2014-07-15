package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static java.io.File.separator;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/** Holds experiment-dependent settings, e.g., number of games per host. */
enum ExperimentConfiguration {

	/** Name of the singleton instance. */
	EXPERIMENT;

	/** Command-line options used in all conditions. */
	final String always;

	/**
	 * Maps condition names to Orego command-line arguments.
	 */
	final Map<String, String> conditions;

	/**
	 * Number of games to play with Orego as each color. The total number of
	 * games will be 2 * <# of hosts> * gamesPerHost * gamesPerColor.
	 */
	final int gamesPerColor;

	/** Total number of games desired per condition. */
	final int gamesPerCondition;

	/**
	 * Number of games to run simultaneously on each host. This should be no
	 * more than the number of processor cores on each host. If Orego is being
	 * run with multiple threads, it might be even smaller.
	 */
	final int gamesPerHost;

	/** Full command to run GNUGo for this experiment. */
	final String gnugo;

	/** Holds board size, komi, and game time. */
	final Rules rules;

	/** Reads settings from config/system.properties. */
	private ExperimentConfiguration() {
		final Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(OREGO_ROOT + separator
					+ "config" + separator + "experiment.properties"));
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		gamesPerHost = parseInt(properties.getProperty("gamesPerHost"));
		gamesPerCondition = parseInt(properties
				.getProperty("gamesPerCondition"));
		gamesPerColor = gamesPerCondition
				/ (2 * SYSTEM.hosts.size() * gamesPerHost);
		if (2 * SYSTEM.hosts.size() * gamesPerHost * gamesPerColor != gamesPerCondition) {
			throw new IllegalArgumentException(
					"Games per condition must be a multiple of 2 * <# of hosts> * <games per host>");
		}
		final int boardSize = parseInt(properties.getProperty("boardSize"));
		final double komi = parseDouble(properties.getProperty("komi"));
		rules = new Rules(boardSize, komi,
				parseInt(properties.getProperty("time")));
		gnugo = SYSTEM.gnugoHome
				+ " --boardsize "
				+ boardSize
				+ " --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi "
				+ komi;
		System.out.println("Gnugo is " + gnugo);
		always = properties.getProperty("always");
		conditions = new TreeMap<>();
		for (final String s : properties.stringPropertyNames()) {
			if (s.startsWith("condition")) {
				conditions.put(s, (String) properties.get(s));
			}
		}
		System.out.println(conditions.size() + " conditions: ");
		for (final String name : conditions.keySet()) {
			System.out.println(name + ": " + conditions.get(name));
		}
	}

}
