package orego.experiment;

import java.io.File;
import java.util.*;

/** Runs GameBatch on each of several machines. */
public class Broadcast {

	public static void main(String[] args) throws Exception {
		Configuration config = new Configuration();
		List<String> hosts = config.getHosts();
		
		Process[] processes = new Process[hosts.size()];
		
		for (int i = 0; i < hosts.size(); i++) {
			String host = hosts.get(i);
			
			ProcessBuilder builder = new ProcessBuilder("nohup", "ssh", host,
					"java -server -ea -cp " + config.getOregoClasspath() + " orego.experiment.GameBatch "
							+ host + "&>" + config.getResultsDirectory().getAbsolutePath() + File.separator + host + ".batch", "&");
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
			new Thread(listener).start();
		}
		for (int i = 0; i < hosts.size(); i++) {
			processes[i].waitFor();
		}
	}

}
