package orego.shape;

import static orego.core.Coordinates.*;
import static orego.core.Board.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;
import orego.core.Colors;
import orego.sgf.SgfParser;
import orego.util.IntSet;

/**
 * This class looks only at patterns that were actually played. These are sorted
 * based on the ratio of patterns seen to patterns played.
 * 
 * @author galbraith
 */

public class PatternCounterTwoPointTwo {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	// public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE +
	// 1;

	// private static final int PATTERN_LENGTH = 24;

	// private static final int PATTERN_STORAGE_CUTOFF = 500000;
	// private static final int PATTERNS_TO_REMOVE = PATTERN_STORAGE_CUTOFF / 2;

	// private static final int PATTERNS_TO_KEEP = (int)Math.pow(2, 16);

	// private static final int SORTED_ARRAY_PATTERN = 0;
	// private static final int SORTED_ARRAY_PLAYED = 1;

	private static final boolean DEBUG = false;

	/**
	 * Long[] will have: 0: frequency seen 1: largest turn 2: lowest turn 3:
	 * total of all turns
	 * */
	private static HashMap<Character, PatternInformation>[][] patternSeen;
	private static HashMap<String, PatternInformation>[][] actualPatternSeen;
	private static String TEST_DIRECTORY = "../../Test Games/";

	// private static String TEST_DIRECTORY = "../../Test Games/";

	public static void main(String[] args) {
		new PatternCounterTwoPointTwo();
	}

	@SuppressWarnings("unchecked")
	public PatternCounterTwoPointTwo() {
		try {
			patternSeen = new HashMap[4][2];
			actualPatternSeen = new HashMap[4][2];
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				for (int j = 0; j < 2; j++) {
					patternSeen[i][j] = new HashMap<Character, PatternInformation>();
					actualPatternSeen[i][j] = new HashMap<String, PatternInformation>();
				}
			}
			setUp(TEST_DIRECTORY);
			
			/*for (int c = 0; c < 2; c++) {
				output = "Color: " + Colors.colorToString(c) + " below.";
				for (int i = 0; i < NINE_PATTERN + 1; i++) {
					Set<Character> initialPatternSeen = patternSeen[i][c]
							.keySet();
					output = "Pattern:" + (i * 2 + 3) + " below.\n";
					bw.write(output);
					System.out.println("Total patterns:"
							+ initialPatternSeen.size());
					for (Character pattern : initialPatternSeen) {
						if (patternSeen[i][c].get(pattern) != null) {
							output = " Total Wins:"
									+ patternSeen[i][c].get(pattern).getRate()
									* patternSeen[i][c].get(pattern).getRuns();
							output += " Total Runs:"
									+ patternSeen[i][c].get(pattern).getRuns();
							output += " Win Rate:"
									+ patternSeen[i][c].get(pattern).getRate();
							output += " " + (i * 2 + 3) + "\n";
							output += patternSeen[i][c].get(pattern).toString();
							bw.write(output);
						}
					}
				}
			}
			System.out.println("Done.");
			bw.write(output);
			System.out.println("Written to file " + TEST_DIRECTORY
					+ "outputPlayed.txt");
			bw.close();*/
			
			PrintWriter bw2 = new PrintWriter(new FileWriter(new File(
					TEST_DIRECTORY + "outputActualPlayed.txt")));
			String output2 = "";
			for (int c = 0; c < 2; c++) {
				output2 = "Color: " + Colors.colorToString(c) + " below.";
				for (int i = 0; i < NINE_PATTERN + 1; i++) {
					PrintWriter bw = new PrintWriter(new FileWriter(new File(
							TEST_DIRECTORY + "outputTableData"+ (i * 2 + 3) +".txt")));
					String output = "";
					Set<String> initialPatternSeen = actualPatternSeen[i][c].keySet();
					output2 = "Pattern:" + (i * 2 + 3) + " below.\n";
					//output = "Pattern:" + (i * 2 + 3) + " below.\n";
					
					bw2.write(output2);
					bw.write(output);
					System.out.println("Total patterns:"
							+ initialPatternSeen.size());
					for (String pattern : initialPatternSeen) {
						if (actualPatternSeen[i][c].get(pattern) != null) {
							output2 = pattern;
							output2 += "Pattern Total Wins:"
									+ actualPatternSeen[i][c].get(pattern).getRate()
									* actualPatternSeen[i][c].get(pattern).getRuns();
							output2 += " Total Runs:"
									+ actualPatternSeen[i][c].get(pattern).getRuns();
							output2 += " Win Rate:"
									+ actualPatternSeen[i][c].get(pattern).getRate();
							output2 += " " + (i * 2 + 3) + "\n";
							//output2 += actualPatternSeen[i][c].get(pattern).toString();
							bw2.write(output2);
							output = actualPatternSeen[i][c].get(pattern).getRate() + "," + actualPatternSeen[i][c].get(pattern).getRuns()+",";
							bw.write(output);
						}
						char patternHash = getHashFromString(i, pattern);
						if (patternSeen[i][c].get(patternHash) != null) {
							output2 = "Table Total Wins:"
									+ patternSeen[i][c].get(patternHash).getRate()
									* patternSeen[i][c].get(patternHash).getRuns();
							output2 += " Total Runs:"
									+ patternSeen[i][c].get(patternHash).getRuns();
							output2 += " Win Rate:"
									+ patternSeen[i][c].get(patternHash).getRate();
							output2 += " " + (i * 2 + 3) + "\n";
							//output2 += patternSeen[i][c].get(patternHash).toString();
							bw2.write(output2);
							output = patternSeen[i][c].get(patternHash).getRate() + "," + patternSeen[i][c].get(patternHash).getRuns()+ "\n";
							bw.write(output);
						}
					}
					System.out.println("Done.");
					//bw.write(output);
					System.out.println("Written to file " + TEST_DIRECTORY
							+ "outputTableData"+ (i * 2 + 3) +".txt");
					bw.close();
				}
			}
			System.out.println("Done.");
			bw2.write(output2);
			System.out.println("Written to file " + TEST_DIRECTORY
					+ "outputActualPlayed.txt");
			bw2.close();


			/*for (int c = 0; c < 2; c++) {
				for (int i = 0; i < NINE_PATTERN + 1; i++) {
					ObjectOutputStream ow = new ObjectOutputStream(
							new FileOutputStream(new File(TEST_DIRECTORY
									+ "patternPlayed" + (i * 2 + 3)
									+ Colors.colorToString(c) + ".dat")));
					ow.writeObject(patternSeen[i][c]);
					ow.flush();
					ow.close();
					System.out.println("Written to file " + TEST_DIRECTORY
							+ "patternPlayed" + (i * 2 + 3)
							+ Colors.colorToString(c) + ".dat");
				}
			}*/
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Takes the hashMap and puts its contents into a two dimensional array of
	 * longs, then sorts the array based on the number of times we have seen a
	 * pattern.
	 * 
	 * @return
	 */
	/*
	 * public PatternInformation[] sortHashMapIntoArray() { PatternInformation[]
	 * toReturn = new PatternInformation[NINE_PATTERN + 1]; for (int i = 0; i <
	 * NINE_PATTERN + 1; i++) { Set<Character> patterns =
	 * patternSeen[i].keySet(); PatternInformation[] sortedArray = new
	 * PatternInformation[patterns.size()]; int index = 0; for(char pattern :
	 * patterns) { sortedArray[index] = patternSeen[i].get(pattern); index++; }
	 * for (int j = 0; j < sortedArray.length - 1; j++) { int maxindex = j+1;
	 * double maxvalue = (sortedArray[j].getRuns()); for (int k = j+1; k <
	 * sortedArray.length; k++) { if (sortedArray[k].getRuns() > maxvalue) {
	 * maxindex = k; maxvalue = sortedArray[k].getRuns(); } } PatternInformation
	 * swapValue = sortedArray[maxindex]; sortedArray[maxindex] =
	 * sortedArray[j]; sortedArray[j] = swapValue; } toReturn = sortedArray; }
	 * return toReturn; }
	 */

	/**
	 * Takes a directory of SGF files and walks through them, counting how often
	 * patterns are seen and played.
	 */
	public void setUp(String filepath) {
		try {
			File directory = new File(filepath);
			System.out.println("Directory: " + directory.getAbsolutePath());
			String[] dirList = directory.list();
			for (int i = 0; i < dirList.length; i++) {
				String filename = filepath + File.separator + dirList[i];
				File file = new File(filename);
				if (file.isDirectory()) {
					setUp(filename);
				} else if (dirList[i].toLowerCase().endsWith(".sgf")) {
					checkPatterns(file);
					// if (DEBUG) System.out.println("Number of patterns:" +
					// patternSeen.size() + " Game:" + i);
					// clearOutNonsense();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Gets rid of uncommon patterns in the hashmap to alleviate memory issues.
	 */
	/*
	 * public void clearOutNonsense() { if (patternSeen.size() >
	 * PATTERN_STORAGE_CUTOFF) { int threshold = 1; while (patternSeen.size() >
	 * PATTERNS_TO_REMOVE) { removePatterns(patternSeen.size() -
	 * PATTERNS_TO_REMOVE, threshold++); } } }
	 */

	/**
	 * Remove patterns seen only once. If toRemove is -1 it will remove all
	 * patterns seen once.
	 * 
	 * @param Number
	 *            of patterns to remove.
	 * @return
	 */
	/*
	 * private int removePatterns(int toRemove, int threshold) { int removed =
	 * 0; Set<Character> patterns = patternSeen.keySet(); Object[] keys =
	 * patterns.toArray(); for(Object key : keys) { Long pattern = (Long)key; if
	 * (patternSeen.get(pattern)[PATTERN_PLAYED] <= threshold) {
	 * patternSeen.remove(pattern); removed++; if (removed >= toRemove &&
	 * toRemove != -1){ break; } } } if (DEBUG)
	 * System.out.println("Removed single patterns:" + removed +
	 * " of size:"+threshold); return removed; }
	 */

	/**
	 * Check for the patterns in a particular file.
	 * 
	 * @param SGF
	 *            file of a particular game.
	 */
	public void checkPatterns(File dir) {
		Board board = SgfParser.sgfToBoard(dir);
		int turn = board.getTurn();
		int currentTurn = 0;
		IntSet possibleMoves;
		int randomMove;
		Board patternBoard = new Board();
		while (currentTurn <= turn) {
			int nextPlay = board.getMove(currentTurn);
			do {
				possibleMoves = patternBoard.getVacantPoints();
				randomMove = possibleMoves
						.get((int) (Math.random() * possibleMoves.size()));
			} while (!patternBoard.isLegal(randomMove)
					|| randomMove == nextPlay);
			if (isOnBoard(nextPlay)) {
				for (int patternType = 0; patternType < NINE_PATTERN + 1; patternType++) {
					long lastPlayHash = patternBoard.getPatternHash(
							patternType, nextPlay);
					if (patternSeen[patternType][patternBoard.getColorToPlay()].containsKey(lastPlayHash)) {
						PatternInformation patternData = patternSeen[patternType][patternBoard.getColorToPlay()]
								.get(lastPlayHash);
						float totalWins = (patternData.getRate() * patternData
								.getRuns());
						patternData.setRuns(patternData.getRuns() + 1);
						patternData.setRate((totalWins + 1)
								/ patternData.getRuns());
					} else {
						patternSeen[patternType][patternBoard.getColorToPlay()].put(
							(char)	lastPlayHash,
								new PatternInformation(1.0f, 1));
					}
					if (hasTwoStones(patternType, patternBoard, nextPlay)) {
						if (actualPatternSeen[patternType][patternBoard.getColorToPlay()].containsKey(patternBoard.printPattern(patternType, nextPlay))) {
							PatternInformation actualPatternData = actualPatternSeen[patternType][patternBoard.getColorToPlay()]
									.get(patternBoard.printPattern(patternType, nextPlay));
							float totalWins2 = (actualPatternData.getRate() * actualPatternData
									.getRuns());
							actualPatternData.setRuns(actualPatternData.getRuns() + 1);
							actualPatternData.setRate((totalWins2 + 1)
									/ actualPatternData.getRuns());
						}
						else {
							actualPatternSeen[patternType][patternBoard.getColorToPlay()].put(
									patternBoard.printPattern(patternType, nextPlay),
									new PatternInformation(1.0f, 1));
						}
					}
					long moveHash = patternBoard.getPatternHash(
							patternType, randomMove);
					if (patternSeen[patternType][patternBoard.getColorToPlay()].containsKey(moveHash)) {
						PatternInformation patternData = patternSeen[patternType][patternBoard.getColorToPlay()]
								.get(moveHash);
						float totalWins = (patternData.getRate() * patternData
								.getRuns());
						patternData.setRuns(patternData.getRuns() + 1);
						patternData.setRate((totalWins)
								/ patternData.getRuns());
					} else {
						patternSeen[patternType][patternBoard.getColorToPlay()].put(
							(char)	moveHash,
								new PatternInformation(0.0f, 1));
					}
					if (hasTwoStones(patternType, patternBoard, randomMove)) {
						if (actualPatternSeen[patternType][patternBoard.getColorToPlay()].containsKey(patternBoard.printPattern(patternType, randomMove))) {
							PatternInformation actualPatternData = actualPatternSeen[patternType][patternBoard.getColorToPlay()]
									.get(patternBoard.printPattern(patternType, randomMove));
							float totalWins2 = (actualPatternData.getRate() * actualPatternData
									.getRuns());
							actualPatternData.setRuns(actualPatternData.getRuns() + 1);
							actualPatternData.setRate((totalWins2)
									/ actualPatternData.getRuns());
						}
						else {
							actualPatternSeen[patternType][patternBoard.getColorToPlay()].put(
									patternBoard.printPattern(patternType, randomMove),
									new PatternInformation(1.0f, 1));
						}
					}
				}
			}
			currentTurn++;
			patternBoard.play(nextPlay);
		}
	}

	private boolean hasTwoStones(int patternType, Board patternBoard, int randomMove) {
		String pattern = patternBoard.printPattern(patternType, randomMove);
		int stonecount = 0;
		for (int i = 0; i < pattern.length(); i++) {
			if (pattern.charAt(i) == '#' || pattern.charAt(i) == 'O' || pattern.charAt(i) == '*') {
				stonecount++;
			}
		}
		return (stonecount == 2);
	}
	
	public char getHashFromString(int patternType, String input) {
		input = input.replace("\n", "");
		char newHash = 0;
		for (int i = 0; i < input.length(); i++) {
			newHash ^= Board.ZOBRIST_PATTERNS[patternType][Colors.charToColor(input.charAt(i))][i];
		}
		return newHash;
	}

}
