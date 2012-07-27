package orego.patternanalyze;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.experiment.Debug.debug;
import static orego.experiment.ExperimentConfiguration.RESULTS_DIRECTORY;
import static orego.patterns.Pattern.*;
import static orego.core.Coordinates.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;

import orego.core.Board;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Cut1Pattern;
import orego.patterns.Pattern;
import orego.patterns.SimplePattern;
import orego.util.BitVector;

public class PatternCounter {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;

	/*
	 * public static final BitVector[] GOOD_NEIGHBORHOODS = { new
	 * BitVector(NUMBER_OF_NEIGHBORHOODS), new
	 * BitVector(NUMBER_OF_NEIGHBORHOODS) };
	 */

	/**
	 * Set of 3x3 patterns taken from Gelly et al,
	 * "Modification of UCT with Patterns in Monte-Carlo Go"
	 */
	private static final String[] PATTERN_STRINGS = {
			// BLACK SPECIFIC PATTERNS
			"O...#O??", // Hane4
			"#??*?O**", // Edge3
			"O?+*?#**", // Edge4
			"O#O*?#**", // Edge5
			// WHITE SPECIFIC PATTERNS
			"O...#O??", // Hane4
			"#??*?O**", // Edge3
			"O?+*?#**", // Edge4
			"O#O*?#**", // Edge5
			// Color independent patterns
			"O..?##??", // Hane1
			"O...#.??", // Hane2
			"O#..#???", // Hane3
			"Cut 1 Pattern",// new Cut1Pattern(), // Cut1
			"#OO+??++", // Cut2
			".O?*#?**", // Edge1
			"#oO*??**", // Edge2
	};

	private static final Pattern[] PATTERN_LIST = {
			// BLACK SPECIFIC PATTERNS
			new ColorSpecificPattern("O...#O??", BLACK), // Hane4
			new ColorSpecificPattern("#??*?O**", BLACK), // Edge3
			new ColorSpecificPattern("O?+*?#**", BLACK), // Edge4
			new ColorSpecificPattern("O#O*?#**", BLACK), // Edge5
			// WHITE SPECIFIC PATTERNS
			new ColorSpecificPattern("O...#O??", WHITE), // Hane4
			new ColorSpecificPattern("#??*?O**", WHITE), // Edge3
			new ColorSpecificPattern("O?+*?#**", WHITE), // Edge4
			new ColorSpecificPattern("O#O*?#**", WHITE), // Edge5
			// Color independent patterns
			new SimplePattern("O..?##??"), // Hane1
			new SimplePattern("O...#.??"), // Hane2
			new SimplePattern("O#..#???"), // Hane3
			new Cut1Pattern(), // Cut1
			new SimplePattern("#OO+??++"), // Cut2
			new SimplePattern(".O?*#?**"), // Edge1
			new SimplePattern("#oO*??**") // Edge2
	};

	private static long[] patternSeen = new long[PATTERN_LIST.length];
	private static long[] patternPlayed = new long[PATTERN_LIST.length];
	private static String TEST_DIRECTORY = "./Test Games/";

	public static void main(String[] args) {
		new PatternCounter();
	}

	public PatternCounter() {
		try {
			setUp(TEST_DIRECTORY);
			PrintWriter bw = new PrintWriter(new FileWriter(new File(
					TEST_DIRECTORY + "output.txt")));
			String output = "";
			for (int i = 0; i < patternSeen.length; i++) {
				output += "Pattern #:" + (i+1);
				output += " Seen:" + patternSeen[i];
				output += " Played:" + patternPlayed[i] + " ";
				output += "Ratio:" + patternPlayed[i] / (1.0 * patternSeen[i])
						+ " ";
				output += PATTERN_STRINGS[i] + "\n";
			}
			System.out.println("Done.");
			bw.write(output);
			bw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Takes a directory of SGF files and walks through them, counting how often patterns are seen and played.
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
	 * @param SGF file of a particular game.
	 */
	public void checkPatterns(File dir) {
		try {
			Board board = new Board();
			String input = "";
			Scanner s = new Scanner(dir);
			while (s.hasNextLine()) {
				input += s.nextLine();
			}
			input = input.replace("W[]", "W[tt]");
			input = input.replace("B[]", "B[tt]");
			StringTokenizer stoken = new StringTokenizer(input, "()[];");
			while (stoken.hasMoreTokens()) {
				String token = stoken.nextToken();
				if (token.equals("W") || token.equals("B")) {
					token = stoken.nextToken();
					if (token.equals("tt")) {
						board.play(PASS);
					} else {
						board.play(sgfToPoint(token));
					}
				}
			}
			s.close();
			int turn = board.getTurn();
			while (turn > 0) {
				int currentPlay = board.getMove(turn);
				int lastPlay = board.getMove(turn - 1);
				if (ON_BOARD[lastPlay] && ON_BOARD[currentPlay]) {
					int[] n = NEIGHBORS[lastPlay];
					for (int i = 0; i < n.length; i++) {
						for (int j = 0; j < PATTERN_LIST.length; j++) {
							if (ON_BOARD[n[i]]
									&& board.getColor(n[i]) == VACANT
									&& PATTERN_LIST[j].matches(board
											.getNeighborhood(n[i]))) {
								patternSeen[j]++;
								if (currentPlay == n[i]) {
									patternPlayed[j]++;
								}
							}
						}
					}
				}
				turn--;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
