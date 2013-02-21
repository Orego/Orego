package orego.experiment.parallel;

import static orego.experiment.ExperimentConfiguration.HOSTS;
import static orego.experiment.ExperimentConfiguration.JAVA_WITH_OREGO_CLASSPATH;
import static orego.experiment.ExperimentConfiguration.RESULTS_DIRECTORY;

import java.util.ArrayList;
import java.util.Scanner;

import orego.experiment.ExperimentConfiguration;

/** Simple class that is very similar to {@link Broadcast} in the
 * main experiment package. This class fires up the remote clients
 * then starts the server.
 * @author samstewart
 *
 */
public class Broadcast {

	/** the directory remote workers will log to*/
	public static final String REMOTE_SEARCHER_RESULTS = "parallel_results/";
			
	public static void main(String[] args) throws Exception {
		spinUpServer();
		
		spinUpRemoteSearchers();
	}
	
	private static void pipeOutput(Process p) throws Exception {
		final Process process = p;
		Runnable listener = new Runnable() {
			public void run() {
				Scanner stdOut = new Scanner(process.getInputStream());
				Scanner errorOut = new Scanner(process.getErrorStream());
				
				while (stdOut.hasNextLine() || errorOut.hasNextLine()) {
					if (stdOut.hasNextLine()) {
						System.out.println(stdOut.nextLine());
					}
					
					if (errorOut.hasNextLine()) {
						System.out.println("[!]" + errorOut.nextLine());
					}
				}
			}
		};
		new Thread(listener).start();
	}
	
	private static void spinUpRemoteSearchers() throws Exception {
		
		ArrayList<Process> processes = new ArrayList<Process>();
		
		for (String remoteHost : ExperimentConfiguration.HOSTS) {
			String java_command = JAVA_WITH_OREGO_CLASSPATH + " -Xmx2048M " + " orego.cluster.ClusterTreeSearcher " + remoteHost + "&> " +
								 RESULTS_DIRECTORY + REMOTE_SEARCHER_RESULTS + remoteHost + ".log";
			
			ProcessBuilder pBuilder = new ProcessBuilder("nohup", "ssh", remoteHost, java_command, "&");
			
			final Process process = pBuilder.start();
			processes.add(process);
			// whip up a listener to pipe the output
			pipeOutput(process);
		}
		
		// make sure we wait for each process
		for (Process process : processes) {
			process.waitFor();
		}
		
	}
	
	private static void spinUpServer() throws Exception {
		// todo: fire up a new game batch on the server machine.
		
	}
}
