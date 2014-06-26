package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.ExperimentConfiguration.EXPERIMENT;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static java.io.File.separator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Plays a series of experimental games on one machine. */
public final class GameBatch implements Runnable {

	/**
	 * @param args
	 *            element 0 is the host name. element 1, if any, is the results
	 *            directory. If none is specified, a new directory in the system
	 *            results directory is created.
	 */
	public static void main(String[] args) {
		assert args.length >= 1;
		String results;
		if (args.length >= 2) {
			results = args[1];
		} else {
			results = SYSTEM.resultsDirectory + timeStamp()
					+ separator;
		}
		new File(results).mkdir();
		try {
			for (int i = 0; i < EXPERIMENT.gamesPerHost; i++) {
				new Thread(new GameBatch(i, args[0], results)).start();
			}
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}

	/** Returns a String representing the current date and time. */
	public static String timeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss.SSS").format(new Date(
				System.currentTimeMillis()));
	}

	/** Number of the batch (used as part of the filename). */
	private final int batchNumber;

	/**
	 * First part (before the first period) of the hostname (used as part of the
	 * filename).
	 */
	private String host;

	private final String resultsDirectory;

	public GameBatch(int batchNumber, String hostname, String resultsDirectory) {
		this.batchNumber = batchNumber;
		this.host = hostname.substring(0, hostname.indexOf('.'));
		this.resultsDirectory = resultsDirectory;
	}

	@Override
	public void run() {
		System.out.println("Running batch " + batchNumber + " on " + host);
		for (String condition : EXPERIMENT.conditions) {
			System.out.println("Batch " + batchNumber + " on " + host + " condition " + condition);
			String orego = SYSTEM.java + " -cp " + SYSTEM.oregoClassPath
					+ " -ea -Xmx1024M edu.lclark.orego.ui.Orego " + condition;
			runGames(orego, EXPERIMENT.gnugo);
			runGames(EXPERIMENT.gnugo, orego);
		}
		System.out.println("Done running batch " + batchNumber + " on " + host);
	}

	/** Runs several games with the specified black and white players. */
	public void runGames(String black, String white) {
		int[] wins = new int[3];
		for (int i = 0; i < EXPERIMENT.gamesPerColor; i++) {
			String outFile = resultsDirectory + host + "-b" + batchNumber + "-"
					+ timeStamp() + ".sgf";
			Game game = new Game(outFile, EXPERIMENT.rules, black, white);
			wins[game.play().index()]++;
		}
	}

}