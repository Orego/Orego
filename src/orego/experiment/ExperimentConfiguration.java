package orego.experiment;

import static orego.core.Coordinates.getBoardWidth;


/** Defines some system-dependent constants for experiments. */
public class ExperimentConfiguration {

	/** Directory where game files are stored. */
	public static final String RESULTS_DIRECTORY = "/Network/Servers/maccsserver.lclark.edu/Users/erikbean/results/";

	/** Command to start Java Virtual Machine with Orego's classpath. */
	public static final String JAVA_WITH_OREGO_CLASSPATH = "java -ea -cp /home/drake/workspace/Orego/bin/";

	/**
	 * The host from which commands are given must be listed first for
	 * KillExperiment to work.
	 */
	public static final String[] HOSTS = { "fido.bw01.lclark.edu"};

	/**
	 * Number of games to run simultaneously on each host. This should be no
	 * more than the number of processor cores on each host. If Orego is being
	 * run with multiple threads, it might be even smaller.
	 */
	public static final int GAMES_PER_HOST = 1;

	/** Total number of games desired per condition. */
	public static final int GAMES_PER_CONDITION = 6;

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
	public static final String[] CONDITIONS = new String[7];

	static {
		CONDITIONS[0] = "threads=2 book=FusekiBook timeformula=uniform c=0.10";
		CONDITIONS[1] = "threads=2 book=FusekiBook timeformula=uniform c=0.15";
		CONDITIONS[2] = "threads=2 book=FusekiBook timeformula=uniform c=0.20";
		CONDITIONS[3] = "threads=2 book=FusekiBook timeformula=basic c=40";
		CONDITIONS[4] = "threads=2 book=FusekiBook timeformula=basic c=50";
		CONDITIONS[5] = "threads=2 book=FusekiBook timeformula=enhanced c=10.0 maxply=80";
		CONDITIONS[6] = "threads=2 book=FusekiBook timeformula=enhanced c=20.0 maxply=80";
	}
	
	/** Path to run gnugo on your machine */
	public static final String GNUGO = "/usr/local/bin/gnugo --boardsize " + getBoardWidth() + " --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi 7.5";

}
