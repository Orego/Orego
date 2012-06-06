package orego.experiment;

import static orego.core.Coordinates.BOARD_WIDTH;

/** Defines some system-dependent constants for experiments. */
public class ExperimentConfiguration {

	public static final String RESULTS_DIRECTORY = "/home/drake/results/";

	public static final String GNUGO = "/usr/local/bin/gnugo --boardsize " + BOARD_WIDTH + " --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi 7.5";
//	public static final String GNUGO = "/usr/local/bin/gnugo --boardsize 9 --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi 7.5";
//////	 public static final String GNUGO =
//////	 "/usr/local/bin/gnugo --mode gtp --quiet --chinese-rules --capture-all-dead --positional-superko --komi 7.5";

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
		 "threads=1 msec=4000",
		 "threads=1 msec=8000",
		 "threads=1 msec=4000 book=FusekiBook",
		 "threads=1 msec=8000 book=FusekiBook",
	 };

//	public static String[] CONDITIONS;
//
//	static {
//		CONDITIONS = new String[0];
//		java.util.ArrayList<String> temp = new java.util.ArrayList<String>();
//		for (int playouts : new int[] { 10000, 20000, 40000, 80000 }) {
//			for (int cutoff : new int[] { 4, 6, 8, 10 }) {
//				for (double learn : new double[] { 0.05, 0.03, 0.01, 0.001, 0.1 }) {
//					temp.add("threads=1 playouts=" + playouts
//							+ " player=orego.neural.Rich1 history=2 learn="
//							+ learn + " cutoff=" + cutoff);
//				}
//				temp.add("threads=1 playouts=" + playouts
//						+ " player=orego.neural.Average history=2 learn=0.05"
//						+ " cutoff=" + cutoff
//				);
//			}
//		}
//		CONDITIONS = temp.toArray(CONDITIONS);
//	}

}
