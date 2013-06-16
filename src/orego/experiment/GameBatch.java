package orego.experiment;

import static orego.core.Colors.*;
import static orego.experiment.ExperimentConfiguration.*;

/** Plays a series of experimental games on one machine. */
public class GameBatch implements Runnable {
	/** prefix (until first dot) of the hostname */
	protected String hostnamePrefix;

	/** Number of the batch (used as part of the filename). */
	private int batchNumber;
	
	/**
	 * 
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: orego.experiment.GameBatch [machine name]");
			System.exit(1);
		}
		
		launchGameBatches(args[0]);
	}
	
	
	
	public static void launchGameBatches(String machineName) {
		try {
			
			for (int i = 0; i < GAMES_PER_HOST; i++) {
				new Thread(new GameBatch(i, machineName)).start();
			}
			
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}


	public GameBatch(int batchNumber, String machine) {
		System.out.println("Creating game batch " + batchNumber + " on " + machine);
		this.batchNumber = batchNumber;
		this.hostnamePrefix = machine.substring(0, machine.indexOf('.'));
	}

	@Override
	public void run() {
		for (String condition : CONDITIONS) {
			String orego = JAVA_WITH_OREGO_CLASSPATH + " -ea -server -Xmx1024M orego.ui.Orego " + condition;
			// run a game where orego is black
			runGames(orego, GNUGO);
			
			// run a game where orego is white
			runGames(GNUGO, orego);
		}
	}

	/** Runs several games with the specified black and white players. 
	 * We use the pre-computed GAMES_PER_COLOR to run the correct amount of games
	 * for a given BLACK and WHITE player.*/
	public void runGames(String black, String white) {
		int[] wins = new int[NUMBER_OF_PLAYER_COLORS];
		
		// no we run all the number of games per color
		for (int i = 0; i < GAMES_PER_COLOR; i++) {
			String fileStem = RESULTS_DIRECTORY +  hostnamePrefix + "-b"
			+ batchNumber + "-" + System.currentTimeMillis();
			Game game;

			game = new Game(fileStem + ".sgf", black, white);
			
			// increment the win counter for the proper player
			wins[game.play()]++;
		}
	}

}
