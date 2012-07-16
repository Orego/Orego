package orego.experiment;

import static orego.experiment.Debug.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs through a batch of experiments and gives the average game length and Std
 * Deviation of winning games and losing games.
 */
public class ExperimentGameLength {

	private static int numWins;
	private static List<Long> numMovesW;
	private static int numLosses;
	private static List<Long> numMovesL;
	private static int numResigns;
	private static List<Long> winStartTimes;
	private static List<Long> lossStartTimes;
	private static List<Long> winEndTimes;
	private static List <Long> lossEndTimes;
	private static int[][] startTimeDist;
	private static List<Double> winPlayoutPerMove;
	private static List<Double> lossPlayoutPerMove;
	private static double[][] playoutDist;

	
	public static final int numPartitions = 10;

	private static String filePath = "/Network/Servers/maccsserver.lclark.edu/Users/sofdev/results3/results3dyntime";

	public static void main(String[] args) {
		try {
			numWins = 0;
			numMovesW = new ArrayList<Long>();
			numLosses = 0;
			numMovesL = new ArrayList<Long>();
			numResigns = 0;
			winStartTimes = new ArrayList<Long>();
			lossStartTimes = new ArrayList<Long>();
			winEndTimes = new ArrayList<Long>();
			lossEndTimes = new ArrayList<Long>();
			winPlayoutPerMove = new ArrayList<Double>();
			lossPlayoutPerMove = new ArrayList<Double>();
			startTimeDist = new int[2][numPartitions];
			playoutDist = new double[2][numPartitions];
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
			long totalTimeW = 0;
			long totalTimeL = 0;
			double aWins;
			double aLosses;
			double sDevW;
			double sDevL;
			for (long moves : numMovesW) {
				totalMovesW += moves;
			}
			for (long moves : numMovesL) {
				totalMovesL += moves;
			}
			long earliestWin = Long.MAX_VALUE;
			long latestWin = 0;
			for (long l: winEndTimes) {
				totalTimeW += l;
				earliestWin = Math.min(earliestWin, l);
				latestWin = Math.max(latestWin, l);
			}
			long earliestLoss = Long.MAX_VALUE;
			long latestLoss = 0;
			for (long l: lossEndTimes) {
				totalTimeL += l;
				earliestLoss = Math.min(earliestLoss, l);
				latestLoss = Math.max(latestLoss, l);
			}
			long earliestStart = Math.min(earliestLoss, earliestWin);
			long latestStart = Math.max(latestLoss, latestWin);
			long startDifference = latestStart-earliestStart;
			ArrayList<ArrayList<Double>> storage = new ArrayList<ArrayList<Double>>();
			for (int i = 0; i < numPartitions; i++) {
				storage.add(new ArrayList<Double>());
			}
			for (int i = 0; i < winEndTimes.size(); i++) {
				
				long fromStart = winEndTimes.get(i)-earliestStart;
				int bucket = (int) (fromStart * (numPartitions-.1) / startDifference);
				startTimeDist[0][bucket]++;
				assert winPlayoutPerMove.size() == winEndTimes.size();
				storage.get(bucket).add(winPlayoutPerMove.get(i));
			}
			for (int i = 0; i < storage.size(); i++) {
				double total = 0;
				for (double playout : storage.get(i)) {
					total += playout;
				}
				playoutDist[0][i] = total / storage.get(i).size();
			}
			storage.clear();
			for (int i = 0; i < numPartitions; i++) {
				storage.add(new ArrayList<Double>());
			}
			for (int i = 0; i < lossEndTimes.size(); i++) {
				long fromStart = lossEndTimes.get(i) - earliestStart;
				int bucket = (int) (fromStart * (numPartitions-.1) / startDifference);
				startTimeDist[1][bucket]++;
				storage.get(bucket).add(lossPlayoutPerMove.get(i));
			}
			for (int i = 0; i < storage.size(); i++) {
				double total = 0;
				for (double playout : storage.get(i)) {
					total += playout;
				}
				playoutDist[1][i] = total / storage.get(i).size();
			}
			double aTimeW = totalTimeW / (double) numWins;
			double aTimeL = totalTimeL / (double) numLosses;
			aWins = totalMovesW / (double) numWins;
			aLosses = totalMovesL / (double) numLosses;
			sDevW = stdDev(aWins, numMovesW);
			sDevL = stdDev(aLosses, numMovesL);
			double timeDevWin = stdDev(aTimeW, winStartTimes);
			double timeDevLoss = stdDev(aTimeL, lossStartTimes);
			System.out.println("Average Moves in a Winning Game: " + aWins
					+ "\nStd Deviation: " + sDevW);
			System.out.println("Average Start Time in a Winning Game: " + aTimeW
					+ "\nStd Deviation: " + timeDevWin + "\nRead " + numWins
					+ " games.");
			System.out.println("Average Moves in a Losing Game: " + aLosses
					+ "\nStd Deviation: " + sDevL);
			System.out.println("Average Start Time in a Losing Game: " + aTimeL
					+ "\nStd Deviation: " + timeDevLoss + "\nRead " + numLosses
					+ " games." + "\nResigned: " + numResigns);
			System.out.println("Average Start Time Differential: " + (aTimeW-aTimeL));
			System.out.println("Wins Distribution: ");
			for (int value : startTimeDist[0]) {
				System.out.print(value + "\t");
			}
			System.out.println("");
			System.out.println("Losses Distributions: ");
			for (int value : startTimeDist[1]) {
				System.out.print(value + "\t");
			}
			System.out.println("");
			System.out.println("Wins Playout Distribution: ");
			for (Double value : playoutDist[0]) {
				System.out.print(value + "\t");
			}
			System.out.println("");
			System.out.println("Losses Playout Distribution: ");
			for (Double value : playoutDist[1]) {
				System.out.print(value + "\t");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void processFile(BufferedReader bf) throws IOException {
		boolean oregoBlack = false;
		boolean winnerBlack = false;
		long moves = 0;
		String line = bf.readLine();
		while (!line.startsWith(")")) {
			if (line.startsWith("PB[j")) {
				oregoBlack = true;
			}
			if (line.startsWith(";")) {
				moves++;
			}
			if (line.startsWith("RE[B")) {
				winnerBlack = true;
			}
			if(winnerBlack != oregoBlack && line.endsWith("+R]")) {
				numResigns++;
			}
			if (line.startsWith("C[playout")) {
				if (oregoBlack == winnerBlack) {
					winPlayoutPerMove.add(Long.decode(line.substring(line.indexOf('=') + 1, line.indexOf(']'))) / (double) moves);
				} else {
					lossPlayoutPerMove.add(Long.decode(line.substring(line.indexOf('=') + 1, line.indexOf(']'))) / (double) moves);
				}
			}
			if (line.startsWith("C[start")) {
				if (winnerBlack == oregoBlack) {
					winStartTimes.add(Long.decode(line.substring(line.indexOf(':')+1, line.indexOf(']'))));
				}
				else {
					lossStartTimes.add(Long.decode(line.substring(line.indexOf(':')+1, line.indexOf(']'))));
				}
			}
			if (line.startsWith("C[end")) {
				if (winnerBlack == oregoBlack) {
					winEndTimes.add(Long.decode(line.substring(line.indexOf(':')+1, line.indexOf(']'))));
				}
				else {
					lossEndTimes.add(Long.decode(line.substring(line.indexOf(':')+1, line.indexOf(']'))));
				}
			}
			line = bf.readLine();
		}
		if (winnerBlack == oregoBlack) {
			numWins++;
			numMovesW.add(moves);
		} else {
			numLosses++;
			numMovesL.add(moves);
		}
	}
	
	public static double stdDev(double mean, List<Long> data) {
		double sumOfSquaredDevs = 0;
		for (long moves : data) {
			sumOfSquaredDevs += ((moves - mean) * (moves - mean));
		}
		sumOfSquaredDevs /= (data.size() - 1);
		return Math.sqrt(sumOfSquaredDevs);
	}
}
