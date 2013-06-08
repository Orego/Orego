package orego.experiment.parallel;

import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.experiment.ExperimentConfiguration.CONDITIONS;
import static orego.experiment.ExperimentConfiguration.PARALLEL_GAMES_PER_COLOR;
import static orego.experiment.ExperimentConfiguration.GAMES_PER_HOST;
import static orego.experiment.ExperimentConfiguration.GNUGO;
import static orego.experiment.ExperimentConfiguration.JAVA_WITH_OREGO_CLASSPATH;
import static orego.experiment.ExperimentConfiguration.RESULTS_DIRECTORY;

import java.io.IOException;
import java.util.ArrayList;

import orego.experiment.ExperimentConfiguration;
import orego.experiment.Game;


public class GameBatch implements Runnable {

	/** entire hostname*/
	protected String hostname;

	/** Number of the batch (used as part of the filename) and for indexing the parallel searchers. */
	private int batchNumber;
	
	/**
	 * Game batch launches a series of threads which then run the individual games per host
	 * so game batch is recursive in this sense.
	 */
	public static void main(String[] args) {
		assert args.length == 1;
		
		launchGameBatches(args[0]);
	}

	public static void launchGameBatches(String hostname) {
		try {
			
			// runs the batches in parallel according to the maximum number of simultaneous games per host
			for (int i = 0; i < GAMES_PER_HOST; i++) {
				new Thread(new orego.experiment.parallel.GameBatch(i, hostname)).start();
			}
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
	}


	public GameBatch(int batchNumber, String machine) {
		this.batchNumber = batchNumber;
		this.hostname = machine;
	}

	@Override
	public void run() {
		  
		// we run a series of games for each of the conditions (synchronously)
		for (String condition : CONDITIONS) {
			String logFile = RESULTS_DIRECTORY + "game_batch_" + this.batchNumber + ".log";
			
			String orego = JAVA_WITH_OREGO_CLASSPATH + " -ea -server -Xmx1024M orego.ui.Orego cluster_player_index=" + this.batchNumber + " cluster_player_log=" + logFile + " " + condition;
			
			// run a game where orego is black. Block until all black games are run.
			runGames(orego, GNUGO);
			
			// run a game where orego is white. Block until all white games are run.
			runGames(GNUGO, orego);
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
		for (int i = 0; i < PARALLEL_GAMES_PER_COLOR; i++) {
			// spin up some clients who will start waiting
			// do this outside of the following loop because we reuse searchers
			spinUpRemoteSearchers();
			
			String fileStem = RESULTS_DIRECTORY +  dirPrefix + "-b" + batchNumber + "-" + System.currentTimeMillis();
			Game game;

			game = new Game(fileStem + ".sgf", black, white);
			
			// increment the win counter for the proper player
			int winner = game.play();
			if(winner >= 0) {
				wins[winner]++;
			}
			else {
				i--;
			}
			
			// now tear down the remote searchers
			tearDownRemoteSearchers();
		}
	}
	
	/** Tears down the remote searchers*/
	private void tearDownRemoteSearchers() {
		
		ArrayList<Process> processes = new ArrayList<Process>();
		
		for (String remoteHost : ExperimentConfiguration.HOSTS) {
			
			String teardown_command = "kill -9 `ps -ef | grep 'ClusterTreeSearcher " + batchNumber + "' | grep -v grep | awk '{print $2}'`";
						
			ProcessBuilder pBuilder = new ProcessBuilder("nohup", "ssh", remoteHost, teardown_command, "&");
			
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
	
	/** Sets up a series of remote searchers with the given player index*/
	private void spinUpRemoteSearchers() {
		ArrayList<Process> processes = new ArrayList<Process>();
		
		for (String remoteHost : ExperimentConfiguration.HOSTS) {
			
			String java_command = JAVA_WITH_OREGO_CLASSPATH + " -Xmx2048M orego.cluster.ClusterTreeSearcher " + batchNumber + " "+ this.hostname + "&> " +
								 RESULTS_DIRECTORY  + remoteHost + "_" + batchNumber + ".log";
						
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
