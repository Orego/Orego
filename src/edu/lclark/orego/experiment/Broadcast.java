package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.SystemConfiguration.*;
import static edu.lclark.orego.experiment.GameBatch.*;
import static java.io.File.separator;

import java.io.File;
import java.util.*;

/** Runs GameBatch on each of several machines. */
public class Broadcast {

	public static void main(String[] args) throws Exception {
		String resultsDirectory = SYSTEM.resultsDirectory + timeStamp() + separator;
		new File(resultsDirectory).mkdir();
		System.out.println("Launching broadcast experiment. Results will be stored in " + resultsDirectory);
		List<String> hosts = SYSTEM.hosts;
		Process[] processes = new Process[hosts.size()];
		for (int i = 0; i < hosts.size(); i++) {
			String host = hosts.get(i);
			// Do not insert spaces in the string "&>" -- bash treats that differently!
			String command = SYSTEM.java + " -cp " + SYSTEM.oregoClassPath
					+ " edu.lclark.orego.experiment.GameBatch " + host
					+ " " + resultsDirectory + "&>"
					+ resultsDirectory + host + ".batch";
			ProcessBuilder builder = new ProcessBuilder("nohup", "ssh", host,
					command, "&");
			builder.redirectErrorStream(true);
			processes[i] = builder.start();
			new Thread(new ProcessTattler(processes[i])).start();
		}
		for (Process p : processes) {
			p.waitFor();
		}
		System.out.println("Broadcast experiment launched.");
	}

}
