package orego.experiment;

import java.io.*;
import java.util.*;
import static orego.experiment.ExperimentConfiguration.*;
import static orego.core.Colors.*;

/** Collates experimental results stored in many files. */
public class Collate {

	public static void main(String[] args) throws FileNotFoundException {
		File dir = new File(RESULTS_DIRECTORY);
		// Maps conditions to results; each result contains the number of wins and the number of games
		Map<String, int[]> results = new TreeMap<String, int[]>();
		// Gather the data
		for (String name : dir.list()) {
			if (name.endsWith(".game")) {
				int[] stats = null;
				int oregoColor = -1;
				Scanner s = new Scanner(new File(RESULTS_DIRECTORY + name));
				while (s.hasNextLine()) {
					String line = s.nextLine();
					if (line.contains("orego.ui.Orego")) {
						// This line specifies Orego's color in this game
						String condition = line.substring(line
								.indexOf("orego.ui.Orego") + 15);
						if (line.startsWith("black:")) {
							oregoColor = BLACK;
						} else {
							oregoColor = WHITE;
						}
						if (results.containsKey(condition)) {
							stats = results.get(condition);
						} else {
							stats = new int[NUMBER_OF_PLAYER_COLORS];
							results.put(condition, stats);
						}
					} else if (line.startsWith("Winner")) {
						if (line.charAt(line.indexOf(' ') + 1) == (oregoColor + '0')) {
							stats[0]++;
						}
						stats[1]++;
					}
				}
				s.close();
			}
		}
		// Print the results
		for (String condition : results.keySet()) {
			int[] stats = results.get(condition);
			System.out.printf(condition + ": %d/%d = %1.3f\n", stats[0],
					stats[1], (((double) (stats[0])) / (stats[1])));
		}
	}

}
