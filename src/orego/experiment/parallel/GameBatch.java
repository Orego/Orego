package orego.experiment.parallel;

import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.experiment.ExperimentConfiguration.CONDITIONS;
import static orego.experiment.ExperimentConfiguration.GAMES_PER_HOST;
import static orego.experiment.ExperimentConfiguration.GNUGO;
import static orego.experiment.ExperimentConfiguration.JAVA_WITH_OREGO_CLASSPATH;
import static orego.experiment.ExperimentConfiguration.PARALLEL_GAMES_PER_COLOR;
import static orego.experiment.ExperimentConfiguration.RESULTS_DIRECTORY;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import orego.experiment.ExperimentConfiguration;
import orego.experiment.Game;


public class GameBatch implements Runnable {

	/** entire hostname*/
	protected String hostname;

	/** Number of the batch (used as part of the filename) and for indexing the parallel searchers. */
	private int batchNumber;
	
	/** batches of games still running*/
	private AtomicInteger batchesRemaining; 
	
	/**
	 * Game batch launches a series of threads which then run the individual games per host
	 * so game batch is recursive in this sense.
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: orego.experiment.parallel.GameBatch [machine name]");
			System.exit(1);
		}
		
		// spin up threads with actual instances of ourselves
		GameBatch.launchGameBatches(args[0]);
	}

	/** Launches more copies of ourself to run the games*/
	public static void launchGameBatches(String hostname) {
		AtomicInteger batchesRemaining = new AtomicInteger();
		batchesRemaining.set(GAMES_PER_HOST);
		
		startRMIRegistry();
		
		try {
			
			// runs the batches in parallel according to the maximum number of simultaneous games per host
			for (int i = 0; i < GAMES_PER_HOST; i++) {
				new Thread(new orego.experiment.parallel.GameBatch(i, hostname, batchesRemaining)).start();
			}
		} catch (Throwable e) {
			e.printStackTrace(System.out);
			System.exit(1);
		}
		
		int batches;
		while ((batches = batchesRemaining.get()) > 0) {
			System.out.println("Waiting 4 minutes for " + batches + " batches to finish up so that we can kill RMI registry");
			
			try {
				Thread.sleep(1000 * 60 * 4);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // only poll every 4 minutes
		}
		
		killRMIRegistry();
	}

	public GameBatch() {
		this.batchNumber = -1;
		this.hostname = null;
	}
	
	public GameBatch(int batchNumber, String machine, AtomicInteger batchesRemaining) {
		this.batchNumber = batchNumber;
		this.hostname = machine;
		this.batchesRemaining = batchesRemaining;
	}

	@Override
	public void run() {
		  
		// we run a series of games for each of the conditions (synchronously)
		for (String condition : CONDITIONS) {
			String logFile = RESULTS_DIRECTORY + "main_controller_log" + this.batchNumber + ".log";
			
			String orego = JAVA_WITH_OREGO_CLASSPATH + " -ea -server -Xmx1024M orego.ui.Orego cluster_player_index=" + this.batchNumber + " cluster_player_log=" + logFile + " " + condition;
			
			// run a game where orego is black. Block until all black games are run.
			runGames(orego, GNUGO);
			
			// run a game where orego is white. Block until all white games are run.
			runGames(GNUGO, orego);
		}
		
		System.out.println("Ended all " + CONDITIONS.length + " games for batch: " + batchNumber);
		
		batchesRemaining.decrementAndGet();
	}

	/** 
	 * Runs several games with the specified black and white players. 
	 * We use the pre-computed GAMES_PER_COLOR to run the correct amount of games
	 * for a given BLACK and WHITE player.
	 */
	public void runGames(String black, String white) {
		int[] wins = new int[NUMBER_OF_PLAYER_COLORS];
		
		String dirPrefix = this.hostname.replace(".", "_");
		
		// no we run all the number of games per color
		for (int i = 0; i < PARALLEL_GAMES_PER_COLOR; i++) {
			System.out.println("Running game: " + i);
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
			
			System.out.println("Ending game: " + i + " with winner: " + winner);
			// now tear down the remote searchers
			tearDownRemoteSearchers();
		}
	}
	
	/** Tears down the remote searchers*/
	private void tearDownRemoteSearchers() {
		
		
		for (String remoteHost : ExperimentConfiguration.HOSTS) {
			System.out.printf("[%d] Force killing: %s\n", batchNumber, remoteHost);
			
			// for this one-liner, thanks to: http://notetodogself.blogspot.com/2006/07/how-to-terminate-process-by-name-in.html 
			String teardown_command = "kill -9 `ps -ef | grep 'ClusterTreeSearcher " + batchNumber + "' | grep -v grep | awk '{print $2}'`";
						
			System.out.println(teardown_command);
			
			ProcessBuilder pBuilder = new ProcessBuilder("nohup", "ssh", remoteHost, teardown_command, "&");
			
			System.out.println("Sent complete kill command: " + pBuilder.command());
			Process process;
			
			try {
				process = pBuilder.start();
				
				process.waitFor();
			} catch (Exception e) {
				System.out.printf("[%d] Could not kill a remote searcher: %s\n", batchNumber, remoteHost);
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.printf("[%d] Successfully killed: %s\n", batchNumber, remoteHost);
		}
	}
	
	private static void killRMIRegistry() {
		
		System.out.println("Killing RMI registry");
		
		ProcessBuilder pBuilder = new ProcessBuilder("killall", "rmiregistry");
		
		Process process;
		
		try {
			process = pBuilder.start();
		
			Thread.sleep(300);
			
		} catch (Exception e) {
			System.out.println("Could not kill RMI registry");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Successfully killed RMI registry");
	}
	
	private static void startRMIRegistry() {
		System.out.println("Starting RMI registry");
		
		ProcessBuilder pBuilder = new ProcessBuilder("nohup", "rmiregistry");
		pBuilder.redirectErrorStream();
		
		Process process;
		
		try {
			process = pBuilder.start();
			
			Thread.sleep(200);
			
		} catch (Exception e) {
			System.out.println("Could not start RMI registry");
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Successfully started RMI registry");
	}
	
	/** Sets up a series of remote searchers with the given player index*/
	private void spinUpRemoteSearchers() {
		
		for (String remoteHost : ExperimentConfiguration.HOSTS) {
			
			String java_command = JAVA_WITH_OREGO_CLASSPATH + " -Xmx2048M orego.cluster.ClusterTreeSearcher " + batchNumber + " " + this.hostname + 
								 " >>" + RESULTS_DIRECTORY  + remoteHost + "_" + batchNumber + ".log" + " 2>&1";
					
			System.out.printf("[%d] Starting searcher on: %s\n", batchNumber, remoteHost);
			
			ProcessBuilder pBuilder = new ProcessBuilder("nohup", "ssh", remoteHost, java_command, "&");
			
			Process process;
			
			try {
				process = pBuilder.start();
				
				process.waitFor();
			} catch (Exception e) {
				System.out.printf("[%d] Could not start a remote searcher: %s\n", batchNumber, remoteHost);
				e.printStackTrace();
				System.exit(1);
			}
			
			
			System.out.printf("[%d] Successfully started searcher on: %s\n", this.batchNumber, remoteHost);
		}
		
	}
}
