package edu.lclark.orego.experiment;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import java.io.IOException;
import java.util.Scanner;

/** Contains static methods for examining the status of the git repository. */
public final class Git {

	/**
	 * Returns the current git commit string, or the empty string if we are not
	 * in a clean git state.
	 */
	public static String getGitCommit() {
		try {
			try (Scanner s = new Scanner(new ProcessBuilder("git", "--git-dir="
					+ OREGO_ROOT + ".git", "--work-tree=" + OREGO_ROOT,
					"status", "-s").start().getInputStream())) {
				if (s.hasNextLine()) {
					System.out.println("There is a next line");
					while (s.hasNextLine()) {
						System.out.println(s.nextLine());
					}
					return "";
				}
			}
			System.out.println("A");
			try (Scanner s = new Scanner(new ProcessBuilder("git", "--git-dir="
					+ OREGO_ROOT + ".git", "--work-tree=" + OREGO_ROOT,
					"status", "log", "--pretty=format:'%H'", "-n", "1").start()
					.getInputStream())) {
				System.out.println("B");
				if (s.hasNextLine()) {
					final String commit = s.nextLine();
					System.out.println("Commit: " + commit);
					// substring to remove single quotes that would otherwise
					// appear
					return commit.substring(1, commit.length() - 1);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return "";
	}

}
