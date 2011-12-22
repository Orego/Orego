package orego.experiment;

import static orego.experiment.ExperimentConfiguration.*;
import java.util.*;

/** Runs GameBatch on each of several machines. */
public class Broadcast {

	public static void main(String[] args) throws Exception {
		Process[] processes = new Process[HOSTS.length];
		for (int i = 0; i < HOSTS.length; i++) {
			String host = HOSTS[i];
			ProcessBuilder builder = new ProcessBuilder("nohup", "ssh", host,
					JAVA_WITH_OREGO_CLASSPATH + " orego.experiment.GameBatch "
							+ host, "&");
			builder.redirectErrorStream(true);
			processes[i] = builder.start();
			final Process p = processes[i];
			Runnable listener = new Runnable() {
				public void run() {
					Scanner fromProgram = new Scanner(p.getInputStream());
					while (fromProgram.hasNextLine()) {
						System.out.println(fromProgram.nextLine());
					}
				}
			};
			new Thread(listener).start();
		}
		for (int i = 0; i < HOSTS.length; i++) {
			processes[i].waitFor();
		}
	}

}
