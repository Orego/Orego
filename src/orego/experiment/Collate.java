package orego.experiment;

import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.experiment.ExperimentConfiguration.RESULTS_DIRECTORY;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

/** 
 * Collates experimental results stored in many files.
 * Dumps the results into a simple CSV format which can be graphed in R or excel.
 */
public class Collate {

	private final static String OREGO_PACKAGE = "orego.ui.Orego";
	
	/** the prefix of the CSV file we'll use for storing stats */
	private static final String CSV_OUTPUT_FILE_PREFIX = "collation_result_";
	
	public static void main(String[] args) throws FileNotFoundException {
		
		// open up the hardcoded path to the result directory
		File dir = new File(RESULTS_DIRECTORY);
		
		// map unique combination of orego parameters to set of stats
		// Stats:
		// 0 - Wins
		// 1 - Runs
		// 2 - Playouts
		// 3 - Moves
		
		Map<String, long[]> results = new TreeMap<String, long[]>();
		
		
		
		// loop through the game data files
		for (String name : dir.list()) {
			
			// only open files containing game data
			if (name.endsWith(".game")) {
				
				// Also check where the numbers are being generated; the same problem may occur there.
				long[] stats = null;
				
				// the color orego is playing as
				char oregoColor = ' ';
				
				// a unique string of parameters for Orego
				String parameters = null;
				
				StringBuilder input = new StringBuilder(); 
				// open the results file
				Scanner s = new Scanner(new File(RESULTS_DIRECTORY + name));
				
				while (s.hasNextLine()) {
					input.append(s.nextLine());
				}
				
				StringTokenizer stoken = new StringTokenizer(input.toString(), "()[];");
				
				while (stoken.hasMoreTokens()) {
					
					String token = stoken.nextToken();
					
					if (token.equals("PB")) { 
						
						// if orego is black, 
						token = stoken.nextToken();
						if (token.contains(OREGO_PACKAGE)) {
							oregoColor = 'B';
							parameters = token.substring(token.indexOf(OREGO_PACKAGE) + OREGO_PACKAGE.length()  + 1);
						}
					}
					
					if (token.equals("PW")) {
						
						// if orego is white
						token = stoken.nextToken();
						if (token.contains(OREGO_PACKAGE)) {
							oregoColor = 'W';
							parameters = token.substring(token.indexOf(OREGO_PACKAGE) + OREGO_PACKAGE.length() + 1);
						}
					}
					if (parameters != null) { 
						
						// if we have already seen a game with the same set of parameters,
						// update our existing information. Otherwise, start a new set of information.
						if (results.containsKey(parameters)) {
							stats = results.get(parameters);
							
						} else {
							stats = new long[NUMBER_OF_PLAYER_COLORS+2];
							results.put(parameters, stats);
						}
					}
					
					// somebody won, who?
					if (token.equals("RE")) { 
						
						token = stoken.nextToken();
						
						// did we win? if so, update our wins counter
						if (token.charAt(0) == oregoColor) {
							stats[0]++;
						}
						
						// did we lose? if so, update our runs counter (do it either way)
						stats[1]++;
					}
					
					// a count command
					if (token.equals("C")) {
						token = stoken.nextToken();
						// count the total number of moves
						if (token.contains("playout")) {
							stats[2] += (Long.parseLong(token.substring(8)));
						}
						
						// count the total number of moves
						if (token.contains("moves")) {
							stats[3] += (Long.parseLong(token.substring(6)));
						}
					}
				}
				s.close();
			}
		}
		
		// Print the results
		printResults(results);
		
		// output the results to a CSV for analysis later
		writeCSVResults(results);
	}
	
	private static void printResults(Map<String, long[]> results) {
		for (String parameters : results.keySet()) {
			long[] stats = results.get(parameters);
			
			// print out the wins/runs ratio
			System.out.printf(parameters+ ": %d/%d = %1.3f\n", stats[0],
					stats[1], (((double) (stats[0])) / (stats[1])));
			
			// print out the total playouts, total moves, average playouts per move
			System.out.printf("Total playouts:%d, Total moves:%d, average playouts per move:%1.3f\n", stats[2], stats[3], (stats[2]/(stats[3]*1.0)));
		}
	}
	
	private static void writeCSVResults(Map<String, long[]> results) {
		File output_csv = new File(RESULTS_DIRECTORY + CSV_OUTPUT_FILE_PREFIX + ".csv");
		
		try {
			// does the output file already exist?
			if (! output_csv.exists()) {
					output_csv.createNewFile();
			} else {
				
				// overwrite it if it does
				output_csv.delete();
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(output_csv));
			
			// write the header
			writer.write("Configuration,Index,Wins,Runs,Win Rate,Total Playouts,Total Moves,Average PPM\n");
			
			// we keep a unique ID for each entry to make stat analysis easier when importing CSV
			int index = 0;
			
			// dump the stats out
			for (String params : results.keySet()) {
				long[] stats = results.get(params);
				
				String line = String.format("%s,%d,%d,%d,%1.3f,%d,%d,%1.3f\n", params, index, stats[0], stats[1], ((double) (stats[0])) / (stats[1]),
																			stats[2], stats[3], (stats[2]/((double) stats[3])));
				writer.write(line);
				
				index++;
			}
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

}
