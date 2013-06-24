package orego.experiment;

import static orego.experiment.Debug.OREGO_ROOT_DIRECTORY;
import static java.io.File.separator;
import static orego.core.Coordinates.getBoardWidth;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


/** Defines some system-dependent constants for experiments. */
public class ExperimentConfiguration {

	/** Directory where game files are stored. */
	public static final String RESULTS_DIRECTORY;

	/** Command to start Java Virtual Machine with Orego's classpath. */
	public static final String JAVA_WITH_OREGO_CLASSPATH;

	/**
	 * The host from which commands are given must be listed first for
	 * KillExperiment to work.
	 */
	public static final String[] HOSTS;
	
	static {
		Properties defaultProp = new Properties();
		try {
			defaultProp.load(new FileInputStream(OREGO_ROOT_DIRECTORY + separator + "config.properties"));
		} catch (FileNotFoundException e1) {
			System.err.println("config.properties not found.");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Properties userProp = new Properties(defaultProp);
		try {
			userProp.load(new FileInputStream(OREGO_ROOT_DIRECTORY + separator + "user.properties"));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		RESULTS_DIRECTORY = userProp.getProperty("resultsdirectory");
		JAVA_WITH_OREGO_CLASSPATH = userProp.getProperty("oregoclasspath");
		GNUGO = userProp.getProperty("gnugoclasspath") + " --boardsize " + getBoardWidth() + " --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi 7.5";
		String s = userProp.getProperty("hosts");
		HOSTS = s.trim().split("\\s+");
	}
	
	/**
	 * Number of games to run simultaneously on each host. This should be no
	 * more than the number of processor cores on each host. If Orego is being
	 * run with multiple threads, it might be even smaller.
	 */
	public static final int GAMES_PER_HOST = 6;

	/** Total number of games desired per condition. */
	public static final int GAMES_PER_CONDITION = 600;

	/**
	 * Number of games to play with Orego as each color. The total number of
	 * games will be 2 * <# of hosts> * GAMES_PER_HOST * GAMES_PER_COLOR.
	 */
	public static final int GAMES_PER_COLOR = GAMES_PER_CONDITION
			/ (2 * HOSTS.length * GAMES_PER_HOST);

	/** The amount of time each player is allocated for each game. */
	public static final int GAME_TIME_IN_SECONDS = 500;
	
	static {
		assert 2 * HOSTS.length * GAMES_PER_HOST * GAMES_PER_COLOR == GAMES_PER_CONDITION : "Games per condition must be a multiple of 2 * <# of hosts> * <games per host>";
	}

	/**
	 * Command line arguments to Orego for the various conditions in the
	 * experiment.
	 */
	public static final String[] CONDITIONS = new String[24];

	static {
		// First test on uniform time formula. c=.5 is our baseline
		CONDITIONS[0] = "threads=2 book=FusekiBook timeformula=uniform c=.50";
		CONDITIONS[1] = "threads=2 book=FusekiBook timeformula=uniform c=.10";
		CONDITIONS[2] = "threads=2 book=FusekiBook timeformula=uniform c=.15";
		CONDITIONS[3] = "threads=2 book=FusekiBook timeformula=uniform c=.20";
		CONDITIONS[4] = "threads=2 book=FusekiBook timeformula=uniform c=.25";
		CONDITIONS[5] = "threads=2 book=FusekiBook timeformula=uniform c=.30";
		CONDITIONS[6] = "threads=2 book=FusekiBook timeformula=uniform c=.35";
		CONDITIONS[7] = "threads=2 book=FusekiBook timeformula=uniform c=.40";
		
		// Second test on basic time formula
		CONDITIONS[8] = "threads=2 book=FusekiBook timeformula=basic c=40";
		CONDITIONS[9] = "threads=2 book=FusekiBook timeformula=basic c=50";
		CONDITIONS[10] = "threads=2 book=FusekiBook timeformula=basic c=60";
		CONDITIONS[11] = "threads=2 book=FusekiBook timeformula=basic c=70";
		CONDITIONS[12] = "threads=2 book=FusekiBook timeformula=basic c=80";
		CONDITIONS[13] = "threads=2 book=FusekiBook timeformula=basic c=90";
		
		// Third time formula, enhanced.
		CONDITIONS[14] = "threads=2 book=FusekiBook timeformula=enhanced c=10 maxply=80";
		CONDITIONS[15] = "threads=2 book=FusekiBook timeformula=enhanced c=20 maxply=80";
		CONDITIONS[16] = "threads=2 book=FusekiBook timeformula=enhanced c=25 maxply=80";
		CONDITIONS[17] = "threads=2 book=FusekiBook timeformula=enhanced c=30 maxply=80";
		CONDITIONS[18] = "threads=2 book=FusekiBook timeformula=enhanced c=35 maxply=80";
		CONDITIONS[19] = "threads=2 book=FusekiBook timeformula=enhanced c=40 maxply=80";
		CONDITIONS[20] = "threads=2 book=FusekiBook timeformula=enhanced c=45 maxply=80";
		CONDITIONS[21] = "threads=2 book=FusekiBook timeformula=enhanced c=50 maxply=80";
		CONDITIONS[22] = "threads=2 book=FusekiBook timeformula=enhanced c=55 maxply=80";
		CONDITIONS[23] = "threads=2 book=FusekiBook timeformula=enhanced c=30 maxply=40";
	}
	
	/** Path to run gnugo on your machine */
	public static final String GNUGO;

}
