package orego.experiment;

import java.io.IOException;
import java.util.*;
import static orego.experiment.ExperimentConfiguration.*;

/** Kills all processes on all hosts listed in orego.experiment.SystemConfiguration.HOSTS. */
public class KillExperiment {

	public static void main(String[] args) {
		try {
			// Make sure the FIRST host in HOSTS is killed last, as it is
			// presumably the machine from which this program is being run.
			String[] hosts = new String[HOSTS.length];
			for (int i = 0; i < HOSTS.length; i++) {
				hosts[i] = HOSTS[(i + 1) % HOSTS.length];
			}
			Process[] processes = new Process[hosts.length];
			for (int i = 0; i < hosts.length; i++) {
				String host = hosts[i];
				ProcessBuilder builder = new ProcessBuilder("ssh", host,
						"kill -9 -1", "&");
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
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
