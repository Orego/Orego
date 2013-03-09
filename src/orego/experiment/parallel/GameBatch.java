package orego.experiment.parallel;

import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import orego.experiment.Configuration;
import orego.experiment.Game;


public class GameBatch implements Runnable {

	/** our handle to the configuration data*/
	private Configuration config;
	
	/** entire hostname*/
	protected String hostname;

	/** Number of the batch (used as part of the filename) and for indexing the parallel searchers. */
	private int batchNumber;
	
	/**
	 * @param args
	 *            element 0 is the host name.
	 */
	public static void main(String[] args) {
		assert args.length == 1;
		
		launchGameBatches(args[0]);
	}
	
	
	
	public static void launchGameBatches(String hostname) {
		try {
			
			Configuration config = new Configuration();
			
			for (int i = 0; i < config.getGamesPerHost(); i++) {
				new Thread(new GameBatch(i, hostname, config)).start();
			}
			
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}


	public GameBatch(int batchNumber, String machine, Configuration config) {
		System.out.println("Creating game batch " + batchNumber + " on " + machine);
		this.batchNumber = batchNumber;
		this.hostname = machine;
		this.config = config;
	}

	@Override
	public void run() {
		// spin up some clients who will start waiting
		// do this outside of the following loop because we reuse searchers
		spinUpRemoteSearchers(this.batchNumber);  
		
		// we run a series of games for each of the conditions (synchronously)
		for (String condition : config.getRunningConditions()) {
			String logFile = config.getResultsDirectory() + File.separator + "game_batch_" + this.batchNumber + ".log";
			
			String orego = "java -cp " + config.getOregoClasspath() + " -ea -server -Xmx1024M orego.ui.Orego cluster_player_index=" + this.batchNumber + " cluster_player_log=" + logFile + " " + condition;
			
			// run a game where orego is black. Block until all black games are run.
			runGames(orego, config.getGnuGoCommand());
			
			// run a game where orego is white. Block until all white games are run.
			runGames(config.getGnuGoCommand(), orego);
		}
	}

	/** 
	 * Runs several games with the specified black and white players. 
	 * We use the pre-computed GAMES_PER_COLOR to run the correct amount of games
	 * for a given BLACK and WHITE player.
	 */
	public void runGames(String black, String white) {
		int[] wins = new int[NUMBER_OF_PLAYER_COLORS];
		
		// only get the prefix before the fist dot to avoid bad file paths
		String dirPrefix = this.hostname.substring(0, this.hostname.indexOf("."));
		
		// no we run all the number of games per color
		for (int i = 0; i < config.getParallelGamesPerColor(); i++) {
			String fileStem = config.getResultsDirectory() + File.separator +  dirPrefix + "-b" + batchNumber + "-" + System.currentTimeMillis();
			Game game;

			game = new Game(fileStem + ".sgf", black, white);
			
			// increment the win counter for the proper player
			wins[game.play()]++;
		}
	}
	
	/** Sets up a series of remote searchers with the given player index*/
	private void spinUpRemoteSearchers(int playerIndex){
		
		ArrayList<Process> processes = new ArrayList<Process>();
		
		for (String remoteHost : config.getHosts()) {
			
			String java_command = "java -ea -server -cp " + config.getOregoClasspath() + " -Xmx2048M orego.cluster.ClusterTreeSearcher " + this.hostname + " " + playerIndex + "&> " +
								 config.getResultsDirectory() + File.separator + remoteHost + "_" + playerIndex + ".log";
						
			ProcessBuilder pBuilder = new ProcessBuilder("nohup", "ssh", remoteHost, java_command, "&");
			
			Process process;
			
			try {
				process = pBuilder.start();
				
				processes.add(process);
			} catch (IOException e) {
				System.out.println("Could not start a remote searcher");
				e.printStackTrace();
				System.exit(1);
			}
			
			
		}
		
		
	}
}
