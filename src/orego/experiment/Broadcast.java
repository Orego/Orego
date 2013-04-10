package orego.experiment;

import static orego.experiment.ExperimentConfiguration.*;
import java.util.*;

/** Runs GameBatch on each of several machines. */
public class Broadcast {

	public static void main(String[] args) throws Exception {
		System.out.println("Broadcasting...");
		Process[] processes = new Process[HOSTS.length];
		for (int i = 0; i < HOSTS.length; i++) {
			String host = HOSTS[i];
			System.out.println("Attempting to start game batch on " + host);
			ProcessBuilder builder = new ProcessBuilder("nohup", "ssh", host,
					JAVA_WITH_OREGO_CLASSPATH + " orego.experiment.GameBatch "
							+ host + "&>" + RESULTS_DIRECTORY + host + ".batch", "&");
			builder.redirectErrorStream(true);
			processes[i] = builder.start();
			final Process p = processes[i];
			Runnable listener = new Runnable() {
				public void run() {
					Scanner stdOut = new Scanner(p.getInputStream());
					Scanner errorOut = new Scanner(p.getErrorStream());
					
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
			System.out.println("Starting process");
			new Thread(listener).start();
			System.out.println("Process started");
		}
		System.out.println("Waiting for processes");
		for (int i = 0; i < HOSTS.length; i++) {
			processes[i].waitFor();
		}
		System.out.println("Done waiting");
	}

}
