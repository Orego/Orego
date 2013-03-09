package orego.experiment;

import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;

import java.io.File;
import java.io.IOException;

/** Plays a series of experimental games on one machine. */
public class GameBatch implements Runnable {
	
	/** our handle to the configuration data*/
	private Configuration config;
	
	/** prefix (until first dot) of the hostname */
	protected String hostnamePrefix;

	/** Number of the batch (used as part of the filename). */
	private int batchNumber;
	
	/**
	 * @param args
	 *            element 0 is the host name.
	 */
	public static void main(String[] args) throws IOException {
		assert args.length == 1;
		
		launchGameBatches(args[0]);
	}
	
	
	
	public static void launchGameBatches(String machineName) throws IOException {
		try {
			Configuration config = new Configuration();
			
			for (int i = 0; i < config.getGamesPerHost(); i++) {
				new Thread(new GameBatch(i, machineName, config)).start();
			}
			
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}


	public GameBatch(int batchNumber, String machine, Configuration config) {
		System.out.println("Creating game batch " + batchNumber + " on " + machine);
		this.batchNumber = batchNumber;
		this.hostnamePrefix = machine.substring(0, machine.indexOf('.'));
		this.config = config;
	}

	@Override
	public void run() {
		for (String condition : this.config.getRunningConditions()) {
			String orego = String.format("java -ea -server -cp %s -Xmx1024M orego.ui.Orego %s", config.getOregoClasspath(), condition);
			// run a game where orego is black
			runGames(orego, config.getGnuGoCommand());
			
			// run a game where orego is white
			runGames(config.getGnuGoCommand(), orego);
		}
	}

	/** Runs several games with the specified black and white players. 
	 * We use the pre-computed GAMES_PER_COLOR to run the correct amount of games
	 * for a given BLACK and WHITE player.*/
	public void runGames(String black, String white) {
		int[] wins = new int[NUMBER_OF_PLAYER_COLORS];
		
		// no we run all the number of games per color
		for (int i = 0; i < config.getGamesPerColor(); i++) {
			String fileStem = config.getResultsDirectory() + File.separator +  hostnamePrefix + "-b"
			+ batchNumber + "-" + System.currentTimeMillis();
			Game game;

			game = new Game(fileStem + ".sgf", black, white);
			
			// increment the win counter for the proper player
			wins[game.play()]++;
		}
	}

}
