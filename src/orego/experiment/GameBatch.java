package orego.experiment;

import static orego.core.Colors.*;
import static orego.experiment.ExperimentConfiguration.*;

/** Plays a series of experimental games on one machine. */
public class GameBatch implements Runnable {
	/** entire hostname*/
	protected String hostname;
	

	/** Number of the batch (used as part of the filename). */
	private int batchNumber;
	
	/**
	 * @param args
	 *            element 0 is the host name.
	 */
	public static void main(String[] args) {
		assert args.length == 1;
		
		launchGameBatches(args[0]);
	}
	
	/** simple gettter for the orego command. We make this protected
	 * so that subclasses can override in the template pattern.
	 * @return the bash command used to run orego.
	 */
	protected String getOregoCommand() {
		return JAVA_WITH_OREGO_CLASSPATH
		+ " -ea -server -Xmx1024M orego.ui.Orego";
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
		this.hostname = machine.substring(0, machine.indexOf('.'));
	}

	@Override
	public void run() {
		for (String condition : CONDITIONS) {
			String orego = this.getOregoCommand() + " " + condition;
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
		
		// only get the prefix before the fist dot to avoid bad file paths
		String dirPrefix = this.hostname.substring(0, this.hostname.indexOf("."));
		
		// no we run all the number of games per color
		for (int i = 0; i < GAMES_PER_COLOR; i++) {
			String fileStem = RESULTS_DIRECTORY +  dirPrefix + "-b"
			+ batchNumber + "-" + System.currentTimeMillis();
			Game game;

			game = new Game(fileStem + ".sgf", black, white);
			
			// increment the win counter for the proper player
			wins[game.play()]++;
		}
	}

}
