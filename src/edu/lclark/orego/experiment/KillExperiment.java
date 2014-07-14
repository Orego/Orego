package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.SystemConfiguration.*;

/** Kills all processes on all hosts listed in system.properties. */
public final class KillExperiment {

	public static void main(String[] args) {
		try {
			// Make sure the FIRST host in HOSTS is killed last, as it is
			// presumably the machine from which this program is being run.
			String[] hosts = new String[SYSTEM.hosts.size()];
			for (int i = 0; i < hosts.length; i++) {
				hosts[i] = SYSTEM.hosts.get((i + 1) % hosts.length);
			}
			final Process[] processes = new Process[hosts.length];
			for (int i = 0; i < hosts.length; i++) {
				// TODO Can we be a little less extreme about this?
				ProcessBuilder builder = new ProcessBuilder("ssh", hosts[i],
						"kill `ps -ef | grep gnugo | awk '{print $2}'`", "&");
				processes[i] = builder.start();
				new Thread(new ProcessTattler(processes[i])).start();
				builder = new ProcessBuilder("ssh", hosts[i],
						"kill `ps -ef | grep java | awk '{print $2}'`", "&");
				processes[i] = builder.start();
				new Thread(new ProcessTattler(processes[i])).start();
			}
			for (int i = 0; i < hosts.length; i++) {
				processes[i].waitFor();
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
