package edu.lclark.orego.experiment;

import java.util.Scanner;

/** Prints any messages from a process to standard output. */
public final class ProcessTattler implements Runnable {

	private final Process process;

	public ProcessTattler(Process process) {
		this.process = process;
	}

	@Override
	public void run() {
		try (Scanner stdOut = new Scanner(process.getInputStream());
				Scanner errorOut = new Scanner(process.getErrorStream())) {
			while (stdOut.hasNextLine() || errorOut.hasNextLine()) {
				if (stdOut.hasNextLine()) {
					System.out.println(stdOut.nextLine());
				}
				if (errorOut.hasNextLine()) {
					System.out.println("[!]" + errorOut.nextLine());
				}
			}
		}
	}

}
