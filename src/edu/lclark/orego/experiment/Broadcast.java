package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.ExperimentConfiguration.EXPERIMENT;
import static edu.lclark.orego.experiment.GameBatch.timeStamp;
import static edu.lclark.orego.experiment.Git.getGitCommit;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static java.io.File.separator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

/**
 * Runs GameBatch on each of several machines.
 */
public final class Broadcast {

	/** Copies all text from source to destination. */
	public static void copyFile(String source, String destination) {
		try (Scanner in = new Scanner(new FileInputStream(source));
				PrintWriter out = new PrintWriter(destination)) {
			while (in.hasNextLine()) {
				out.println(in.nextLine());
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {
		final String gitCommit = getGitCommit();
		if (gitCommit.isEmpty()) {
			throw new IllegalStateException("Not in clean git state");
		}
		System.out.println("Preparing to launch "
				+ EXPERIMENT.gamesPerCondition * EXPERIMENT.conditions.size()
				+ " games");
		// The 0.65 below is an empirical factor to get us a more accurate
		// estimate of how long the experiment will take
		System.out.println("Estimated time (hours) : " + 0.70
				* EXPERIMENT.gamesPerCondition * EXPERIMENT.conditions.size()
				* EXPERIMENT.rules.time * 2
				/ (SYSTEM.hosts.size() * EXPERIMENT.gamesPerHost * 3600.0));
		final String resultsDirectory = SYSTEM.resultsDirectory
				+ timeStamp(true) + separator;
		System.out
				.println("Launching broadcast experiment. Results will be stored in "
						+ resultsDirectory);
		new File(resultsDirectory).mkdirs();
		copyFile(OREGO_ROOT + "config" + separator + "system.properties",
				resultsDirectory + "system.txt");
		copyFile(OREGO_ROOT + "config" + separator + "experiment.properties",
				resultsDirectory + "experiment.txt");
		try (PrintWriter out = new PrintWriter(resultsDirectory + "git.txt")) {
			out.println(gitCommit);
		}
		final List<String> hosts = SYSTEM.hosts;
		final Process[] processes = new Process[hosts.size()];
		for (int i = 0; i < hosts.size(); i++) {
			final String host = hosts.get(i);
			// Create results directory on host, which might be on a different file system
			new ProcessBuilder("nohup", "ssh", host, "-A", "-o StrictHostKeyChecking=no", "-o UserKnownHostsFile=/dev/null", "mkdir -p " + resultsDirectory).start().waitFor();
			System.out.println("Starting games on " + host);
			// Do not insert spaces in the string "&>" -- bash treats that
			// differently!
			final String command = SYSTEM.java + " -ea -cp "
					+ SYSTEM.oregoClassPath
					+ " edu.lclark.orego.experiment.GameBatch " + host + " "
					+ resultsDirectory + "&>" + resultsDirectory + host
					+ ".batch";
			final ProcessBuilder builder = new ProcessBuilder("nohup", "ssh",
					host, "-A", "-o StrictHostKeyChecking=no", "-o UserKnownHostsFile=/dev/null", command, "&");
			builder.redirectErrorStream(true);
			processes[i] = builder.start();
			new Thread(new ProcessTattler(processes[i])).start();
		}
		for (final Process p : processes) {
			p.waitFor();
		}
		System.out.println("Broadcast experiment launched.");
	}
}
