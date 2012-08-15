package orego.experiment;

import static orego.core.Coordinates.BOARD_WIDTH;

/** Defines some system-dependent constants for experiments. */
public class ExperimentConfiguration {

	/** Directory where game files are stored. */
	public static final String RESULTS_DIRECTORY = "/home/drake/results/";

	/** Path to run GNUGO on your machine. */
	public static final String GNUGO = "/usr/local/bin/gnugo --boardsize " + BOARD_WIDTH + " --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi 7.5";

	/** Command to start Java Virtual Machine with Orego's classpath. */
	public static final String JAVA_WITH_OREGO_CLASSPATH = "java -ea -cp /home/drake/git/Orego/Orego/bin";

	/**
	 * The host from which commands are given must be listed first for
	 * KillExperiment to work.
	 */
	public static final String[] HOSTS = { "fido.bw01.lclark.edu",
			"n001.bw01.lclark.edu",
			"n002.bw01.lclark.edu",
			"n003.bw01.lclark.edu", "n004.bw01.lclark.edu" };

	/**
	 * Number of games to run simultaneously on each host. This should be no
	 * more than the number of processor cores on each host. If Orego is being
	 * run with multiple threads, it might be even smaller.
	 */
	public static final int GAMES_PER_HOST = 12;

	/** Total number of games desired per condition. */
	public static final int GAMES_PER_CONDITION = 600;

	/**
	 * Number of games to play with Orego as each color. The total number of
	 * games will be 2 * <# of hosts> * GAMES_PER_HOST * GAMES_PER_COLOR.
	 */
	public static final int GAMES_PER_COLOR = GAMES_PER_CONDITION
			/ (2 * HOSTS.length * GAMES_PER_HOST);

	static {
		assert 2 * HOSTS.length * GAMES_PER_HOST * GAMES_PER_COLOR == GAMES_PER_CONDITION : "Games per condition must be a multiple of 2 * <# of hosts> * <games per host>";
	}

	/**
	 * Command line arguments to Orego for the various conditions in the
	 * experiment.
	 */
	 public static final String[] CONDITIONS = {
		 "threads=1 msec=4000 book=FusekiBook heuristics=Escape@20:Pattern@20:Capture@20"
	 };

}
