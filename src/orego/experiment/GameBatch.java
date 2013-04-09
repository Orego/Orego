package orego.experiment;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.experiment.ExperimentConfiguration.*;

/** Plays a series of experimental games on one machine. */
public class GameBatch implements Runnable {

	/** Command to run Orego, but without command line arguments. */
	public static final String OREGO_BASE = JAVA_WITH_OREGO_CLASSPATH
			+ " -ea -server -Xmx1024M orego.ui.Orego";

	/**
	 * @param args
	 *            element 0 is the host name.
	 */
	public static void main(String[] args) {
		try {
			System.out.println(java.util.Arrays.toString(args));
			assert args.length == 1;
			for (int i = 0; i < GAMES_PER_HOST; i++) {
				new Thread(new GameBatch(i, args[0])).start();
			}
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}

	/** Number of the batch (used as part of the filename). */
	private int batchNumber;

	/**
	 * First part (before the first period) of the hostname (used as part of the
	 * filename).
	 */
	private String machine;

	public GameBatch(int batchNumber, String machine) {
		System.out.println("Creating game batch " + batchNumber + " on " + machine);
		this.batchNumber = batchNumber;
		this.machine = machine.substring(0, machine.indexOf('.'));
	}

	@Override
	public void run() {
		for (String condition : CONDITIONS) {
			String orego = OREGO_BASE + " " + condition;
			runGames(orego, GNUGO);
			runGames(GNUGO, orego);
		}
	}

	/** Runs several games with the specified black and white players. */
	public void runGames(String black, String white) {
		int[] wins = new int[NUMBER_OF_PLAYER_COLORS];
		for (int i = 0; i < GAMES_PER_COLOR; i++) {
			String fileStem = RESULTS_DIRECTORY + machine + "-b"
			+ batchNumber + "-" + System.currentTimeMillis();
			Game game;
//			if (black.contains("Orego")) {
//				game = new Game(fileStem + ".game", black + " debugfile=" + fileStem + ".debug", white);
//			} else {
				game = new Game(fileStem + ".sgf", black, white);				
//			}
			wins[game.play()]++;
		}
	}

}
