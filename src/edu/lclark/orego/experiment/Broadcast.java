package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static edu.lclark.orego.experiment.SystemConfiguration.*;
import static edu.lclark.orego.experiment.ExperimentConfiguration.*;
import static edu.lclark.orego.experiment.GameBatch.*;
import static java.io.File.separator;

import java.io.*;
import java.util.*;

/** Runs GameBatch on each of several machines. */
public final class Broadcast {

	public static void main(String[] args) throws Exception {
		System.out.println("Preparing to launch "
				+ (EXPERIMENT.gamesPerCondition * EXPERIMENT.conditions.size())
				+ " games");
		String resultsDirectory = SYSTEM.resultsDirectory + timeStamp()
				+ separator;
		System.out
				.println("Launching broadcast experiment. Results will be stored in "
						+ resultsDirectory);
		new File(resultsDirectory).mkdir();
		copyFile(OREGO_ROOT + "config" + separator + "system.properties",
				resultsDirectory + "system.txt");
		copyFile(OREGO_ROOT + "config" + separator + "experiment.properties",
				resultsDirectory + "experiment.txt");
		writeGitCommit(resultsDirectory + "git.txt");
		List<String> hosts = SYSTEM.hosts;
		Process[] processes = new Process[hosts.size()];
		for (int i = 0; i < hosts.size(); i++) {
			String host = hosts.get(i);
			// Do not insert spaces in the string "&>" -- bash treats that
			// differently!
			String command = SYSTEM.java + " -ea -cp " + SYSTEM.oregoClassPath
					+ " edu.lclark.orego.experiment.GameBatch " + host + " "
					+ resultsDirectory + "&>" + resultsDirectory + host
					+ ".batch";
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

	/**
	 * Determines the current git commit and writes it to filename. If we are
	 * not in a clean git state, throws an IllegalStateException.
	 */
	private static void writeGitCommit(String filename) {
		// Verify that we are in a clean git state
		try (Scanner s = new Scanner(new ProcessBuilder("git", "status", "-s").start().getInputStream())) {
			if (s.hasNextLine()) {
				throw new IllegalStateException("Not in clean git state");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// Write commit to a file
		try (Scanner s = new Scanner(new ProcessBuilder("git", "log", "--pretty=format:'%H'", "-n", "1").start().getInputStream())) {
			String commit = s.nextLine();
			try (PrintWriter out = new PrintWriter(filename)) {
				// substring to remove single quotes that would otherwise appear
				out.println(commit.substring(1, commit.length() - 1));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void copyFile(String source, String destination) {
		try (Scanner in = new Scanner(new FileInputStream(source));
				PrintWriter out = new PrintWriter(destination)) {
			while (in.hasNextLine()) {
				out.println(in.nextLine());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
