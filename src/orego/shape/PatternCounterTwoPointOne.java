package orego.shape;

import static orego.core.Board.*;
import static orego.core.Coordinates.*;
import static orego.shape.PatternPlayer.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

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

public class PatternCounterTwoPointOne {

	/**
	 * Long[] will have: 0: frequency seen 1: largest turn 2: lowest turn 3:
	 * total of all turns
	 * */
	private static HashMap<Character, PatternInformation>[][][] patternSeen;
	private static String TEST_DIRECTORY = ".."+File.separator+".."+File.separator+".."+File.separator+"Desktop"+File.separator+"Test Games"+File.separator;//+"kgs-19-2001"+File.separator;
	
	// private static String TEST_DIRECTORY = "../../Test Games/";

	public static void main(String[] args) {
		new PatternCounterTwoPointOne();
	}

	@SuppressWarnings("unchecked")
	public PatternCounterTwoPointOne() {
		try {
			patternSeen = new HashMap[NUM_HASH_TABLES][MAX_PATTERN_RADIUS+1][2];
			for (int i = 0; i < MAX_PATTERN_RADIUS + 1; i++) {
				for (int j = 0; j < 2; j++) {
					for (int k = 0; k < NUM_HASH_TABLES; k++) {
						patternSeen[k][i][j] = new HashMap<Character, PatternInformation>();
					}
				}
			}
			setUp(TEST_DIRECTORY);
			PrintWriter bw = new PrintWriter(new FileWriter(new File(
					TEST_DIRECTORY + "outputPlayed.txt")));
			String output = "";
			bw.write("Warning: this only has info on first of four hash tables");
			for (int c = 0; c < 2; c++) {
				output = "Color: " + Colors.colorToString(c) + " below.";
				for (int i = 0; i < MAX_PATTERN_RADIUS + 1; i++) {
					Set<Character> initialPatternSeen = patternSeen[0][i][c]
								.keySet();
					output = "Pattern:" + (i * 2 + 3) + " below.\n";
					bw.write(output);
					System.out.println("Total patterns:"
							+ initialPatternSeen.size());
					for (Character pattern : initialPatternSeen) {
						if (patternSeen[0][i][c].get(pattern) != null) {
							output = " Total Wins:"
									+ patternSeen[0][i][c].get(pattern)
											.getRate()
									* patternSeen[0][i][c].get(pattern)
											.getRuns();
							output += " Total Runs:"
									+ patternSeen[0][i][c].get(pattern)
											.getRuns();
							output += " Win Rate:"
									+ patternSeen[0][i][c].get(pattern)
											.getRate();
							output += " " + (i * 2 + 3) + "\n";
							output += patternSeen[0][i][c].get(pattern)
									.toString();
							bw.write(output);
						}
					}
				}
			}
			System.out.println("Done.");
			bw.write(output);
			System.out.println("Written to file " + TEST_DIRECTORY
					+ "outputPlayed.txt");
			bw.close();
			for (int c = 0; c < 2; c++) {
				for (int i = 0; i < MAX_PATTERN_RADIUS + 1; i++) {
					for (int t = 0; t < NUM_HASH_TABLES; t++) {
						ObjectOutputStream ow = new ObjectOutputStream(
								new FileOutputStream(new File(TEST_DIRECTORY
										+ "patternPlayed" + (i * 2 + 3)
										+ Colors.colorToString(c) + t + ".dat")));
						ow.writeObject(patternSeen[t][i][c]);
						ow.flush();
						ow.close();
						System.out.println("Written to file " + TEST_DIRECTORY
								+ "patternPlayed" + (i * 2 + 3)
								+ Colors.colorToString(c) + t + ".dat");
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

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
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

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
				for (int patternType = 0; patternType < MAX_PATTERN_RADIUS + 1; patternType++) {
					for (int table = 0; table < NUM_HASH_TABLES; table++) {
						long lastPlayHash = patternBoard.getPatternHash(
								patternType, nextPlay);
						if (patternSeen[table][patternType][patternBoard
								.getColorToPlay()].containsKey(hashLongToChar(
								lastPlayHash, table))) {
							PatternInformation patternData = patternSeen[table][patternType][patternBoard
									.getColorToPlay()].get(hashLongToChar(
									lastPlayHash, table));
							float totalWins = (patternData.getRate() * patternData
									.getRuns());
							patternData.setRuns(patternData.getRuns() + 1);
							patternData.setRate((totalWins + 1)
									/ patternData.getRuns());
						} else {
							patternSeen[table][patternType][patternBoard
									.getColorToPlay()].put(
									hashLongToChar(lastPlayHash, table),
									new PatternInformation(1.0f, 1));
						}
						long moveHash = patternBoard.getPatternHash(
								patternType, randomMove);
						if (patternSeen[table][patternType][patternBoard
								.getColorToPlay()].containsKey(hashLongToChar(
										moveHash, table))) {
							PatternInformation patternData = patternSeen[table][patternType][patternBoard
									.getColorToPlay()].get(hashLongToChar(
									moveHash, table));
							float totalWins = (patternData.getRate() * patternData
									.getRuns());
							patternData.setRuns(patternData.getRuns() + 1);
							patternData.setRate((totalWins)
									/ patternData.getRuns());
						} else {
							patternSeen[table][patternType][patternBoard
									.getColorToPlay()].put(
									hashLongToChar(moveHash, table),
									new PatternInformation(0.0f, 1));
						}
					}
				}
			}
			currentTurn++;
			patternBoard.play(nextPlay);
		}
	}
}
