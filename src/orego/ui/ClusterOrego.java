package orego.ui;

import static orego.experiment.ExperimentConfiguration.JAVA_WITH_OREGO_CLASSPATH;
import static orego.experiment.ExperimentConfiguration.HOSTS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClusterOrego {
	
	public static void main(String[] args) throws IOException {
		List<String> modifiedArgs = new ArrayList<String>();
		
		String localhost = null;
		String output = null;
		String player = "ClusterPlayer";
		
		for (int i = 0; i < args.length; i++) {
			String argument = args[i];
			// Split argument at the equals sign
			int j = argument.indexOf('=');
			String left, right;
			if (j > 0) {
				left = argument.substring(0, j);
				right = argument.substring(j + 1);
			} else {
				left = argument;
				right = "true";
			}
			if(left.equals("player")) {
				player = right;
			}
			else if(left.equals("localhost")) {
				localhost = right;
			}
			else if(left.equals("output")) {
				output = right;
			}
			else {
				modifiedArgs.add(argument);
			}
		}
		
		if(localhost == null || output == null) {
			System.out.println("Usage: ClusterOrego localhost=localhost.name output=/some/output/dir [orego arguments]");
			System.exit(1);
		}
		
		modifiedArgs.add("player=" + player);
		modifiedArgs.add("cluster_player_log=" + new File(output, "player.log").getPath());
		
		List<Process> searchers = spinUpRemoteSearchers(localhost, output);
		
		String[] oregoArgs = new String[modifiedArgs.size()];
		modifiedArgs.toArray(oregoArgs);
		Orego.main(oregoArgs);
		
		reapSearchers(searchers);
	
	}

	private static List<Process> spinUpRemoteSearchers(String localhost, String outputDirectory) {
		
		ArrayList<Process> processes = new ArrayList<Process>();

		for (String remoteHost : HOSTS) {

			String java_command = String.format("%s -Xmx2048M orego.cluster.ClusterTreeSearcher -1 %s &> %s%s.log", 
								JAVA_WITH_OREGO_CLASSPATH, 
								localhost, 
								outputDirectory, 
								remoteHost);

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

		return processes;
	}
	
	private static void reapSearchers(List<Process> searchers) {
		for(Process searcher : searchers) {
			searcher.destroy();
		}
	}
	
}
