package orego.experiment;

import static orego.experiment.Debug.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Runs through a batch of experiments and gives the average game length and Std
 * Deviation of winning games and losing games.
 */
public class ExperimentGameLength {

	private static int numWins;
	private static ArrayList<Integer> numMovesW;
	private static int numLosses;
	private static ArrayList<Integer> numMovesL;
	private static int numResigns;

	private static String filePath = "/Network/Servers/maccsserver.lclark.edu/Users/sofdev/results1";

	public static void main(String[] args) {
		try {
			numWins = 0;
			numMovesW = new ArrayList<Integer>();
			numLosses = 0;
			numMovesL = new ArrayList<Integer>();
			numResigns = 0;
			File directory = new File(filePath);
			String[] dirList = directory.list();
			// for (int i = 0; i < dirList.length; i++) {
			for (int i = 0; i < dirList.length; i++) {

				String filename = filePath + File.separator + dirList[i];
				// System.out.println(filename);
				File file = new File(filename);
				if (file.isDirectory()) {
					// setUp(filename);
				} else if (dirList[i].toLowerCase().endsWith(".game")) {
					debug("Processing " + dirList[i]);
					FileReader reader = new FileReader(file);
					BufferedReader bf = new BufferedReader(reader);
					processFile(bf);
					reader.close();
				}
			}
			int totalMovesW = 0;
			int totalMovesL = 0;
			double aWins;
			double aLosses;
			double sDevW;
			double sDevL;
			for (int moves : numMovesW) {
				totalMovesW += moves;
			}
			for (int moves : numMovesL) {
				totalMovesL += moves;
			}
			aWins = totalMovesW / (double) numWins;
			aLosses = totalMovesL / (double) numLosses;
			double sumOfSquaredDevs = 0;
			for (int moves : numMovesW) {
				sumOfSquaredDevs += ((moves - aWins) * (moves - aWins));
			}
			sumOfSquaredDevs /= (numWins - 1);
			sDevW = Math.sqrt(sumOfSquaredDevs);
			sumOfSquaredDevs = 0;
			for (int moves : numMovesL) {
				sumOfSquaredDevs += ((moves - aLosses) * (moves - aLosses));
			}
			sumOfSquaredDevs /= (numLosses - 1);
			sDevL = Math.sqrt(sumOfSquaredDevs);

			System.out.println("Average Moves in a Winning Game: " + aWins
					+ "\nStd Deviation: " + sDevW + "\nRead " + numWins
					+ " games.");
			System.out.println("Average Moves in a Losing Game: " + aLosses
					+ "\nStd Deviation: " + sDevL + "\nRead " + numLosses
					+ " games." + "\nResigned: " + numResigns);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void processFile(BufferedReader bf) throws IOException {
		boolean oregoBlack = false;
		boolean winnerBlack = false;
		int moves = 0;
		String line = bf.readLine();
		while (!line.startsWith("RE[")) {
			if (line.startsWith("PB[j")) {
				oregoBlack = true;
			}
			if (line.startsWith(";")) {
				moves++;
			}
			line = bf.readLine();
		}
		if (line.startsWith("RE[B")) {
			winnerBlack = true;
		}
		if (winnerBlack == oregoBlack) {
			numWins++;
			numMovesW.add(moves);
		} else {
			numLosses++;
			numMovesL.add(moves);
			if(line.endsWith("+R]")){
				numResigns++;
			}
		}
	}
}
