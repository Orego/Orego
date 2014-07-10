package edu.lclark.orego.experiment;

import java.io.IOException;
import java.util.Scanner;

/** Contains static methods for examining the status of the git repository. */
public class Git {

	/**
	 * Returns the current git commit string, or the empty string if we are not
	 * in a clean git state.
	 */
	public static String getGitCommit() {
		try {
			try (Scanner s = new Scanner(new ProcessBuilder("git", "status",
					"-s").start().getInputStream())) {
				if (s.hasNextLine()) {
					return "";
				}
			}
			try (Scanner s = new Scanner(new ProcessBuilder("git", "log",
					"--pretty=format:'%H'", "-n", "1").start().getInputStream())) {
				if (s.hasNextLine()) {
					String commit = s.nextLine();
					// substring to remove single quotes that would otherwise
					// appear
					return commit.substring(1, commit.length() - 1);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return "";
	}

}
