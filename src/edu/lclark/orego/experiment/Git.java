package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import java.io.IOException;
import java.util.Scanner;

/** Contains static methods for examining the status of the git repository. */
public final class Git {

	/**
	 * Returns the current git commit string, or the empty string if we are not
	 * in a clean git state. If run on a Windows machine, it may not be possible
	 * to view the current git commit string.
	 */
	public static String getGitCommit() {
		try {
			try (Scanner s = new Scanner(new ProcessBuilder("git", "--git-dir="
					+ OREGO_ROOT + ".git", "--work-tree=" + OREGO_ROOT,
					"status", "-s").start().getInputStream())) {
				if (s.hasNextLine()) {
					return "";
				}
			}
			try (Scanner s = new Scanner(new ProcessBuilder("git", "--git-dir="
					+ OREGO_ROOT + ".git", "--work-tree=" + OREGO_ROOT, "log",
					"--pretty=format:'%H'", "-n", "1").start().getInputStream())) {
				if (s.hasNextLine()) {
					final String commit = s.nextLine();
					// substring to remove single quotes that would otherwise
					// appear
					return commit.substring(1, commit.length() - 1);
				}
			}
		} catch (final IOException e) {
			return "unknown (git not installed)";
		}
		return "";
	}

}
