package orego.experiment;

import java.io.*;
import java.util.*;
import static orego.core.Colors.*;

// TODO There should be an easy way to dump these data into a graph
/** Collates experimental results stored in many files. */
public class Collate {

	public static void main(String[] args) throws Exception {
		Configuration config = new Configuration();
		
		File dir = config.getResultsDirectory();
		// Maps conditions to results; each result contains the number of wins and the number of games
		Map<String, int[]> results = new TreeMap<String, int[]>();
		// total playouts and moves
		// Gather the data
		for (String name : dir.list()) {
			if (name.endsWith(".sgf")) {
				// TODO This should be an array of longs; ints overflow
				// Also check where the numbers are being generated; the same problem may occur there.
				int[] stats = null;
				char oregoColor = ' ';
				String input = "";
				String condition = null;
				Scanner s = new Scanner(new File(config.getResultsDirectory().getAbsolutePath() + File.separator + name));
				while (s.hasNextLine()) {
					input += s.nextLine();
				}
				StringTokenizer stoken = new StringTokenizer(input, "()[];");
				while (stoken.hasMoreTokens()) {
					String token = stoken.nextToken();
					if (token.equals("PB")) { //If the player is black
						token = stoken.nextToken();
						if (token.contains("orego.ui.Orego")) {
							oregoColor = 'B';
							condition = token.substring(token.indexOf("orego.ui.Orego") + 15);
						}
					}
					if (token.equals("PW")) { //If the player is white
						token = stoken.nextToken();
						if (token.contains("orego.ui.Orego")) {
							oregoColor = 'W';
							condition = token.substring(token.indexOf("orego.ui.Orego") + 15);
						}
					}
					if (condition != null) { //Set the appropriate stat array
						if (results.containsKey(condition)) {
							stats = results.get(condition);
						} else {
							stats = new int[NUMBER_OF_PLAYER_COLORS+2];
							results.put(condition, stats);
						}
					}
					if (token.equals("RE")) { //Find the winner
						token = stoken.nextToken();
						if (token.charAt(0) == oregoColor) {
							stats[0]++;
						}
						stats[1]++;
					}
					if (token.equals("C")) {
						token = stoken.nextToken();
						if (token.contains("playout")) {
							stats[2] += (Long.parseLong(token.substring(8)));
						}
						if (token.contains("moves")) {
							stats[3] += (Long.parseLong(token.substring(6)));
						}
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
			System.out.printf("Total playouts:%d, Total moves:%d, average playouts per move:%1.3f\n", stats[2], stats[3], (stats[2]/(stats[3]*1.0)));
		}
	}

}
