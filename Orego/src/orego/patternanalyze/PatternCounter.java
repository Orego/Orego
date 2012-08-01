package orego.patternanalyze;

import static orego.core.Coordinates.*;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;

public class PatternCounter {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;

	private static final int PATTERN_LENGTH = 8;
	
	private static final int PATTERN_STORAGE_CUTOFF = 5000;
	private static final int PATTERNS_TO_REMOVE = PATTERN_STORAGE_CUTOFF / 2;

	/**
	 * Long[] will have:
	 * 0: frequency seen
	 * 1: largest turn
	 * 2: lowest turn
	 * 3: total of all turns
	 * */
	private static HashMap<Long, Long[]> patternSeen = new HashMap<Long, Long[]>();
	private static String TEST_DIRECTORY = "../../../Test Games/kgs-/";

	public static void main(String[] args) {
		new PatternCounter();
	}

	public PatternCounter() {
		try {
			setUp(TEST_DIRECTORY);
			PrintWriter bw = new PrintWriter(new FileWriter(new File(
					TEST_DIRECTORY + "output"+PATTERN_LENGTH+".txt")));
			String output = "";
			Long[][] initialPatternSeen = sortHashMapIntoArray();
			for (Long[] pattern : initialPatternSeen) {
				output = "Seen:" + patternSeen.get(pattern[0])[0];
				output += " Played:" + patternSeen.get(pattern[0])[4];
				output += " Ratio:" +  (patternSeen.get(pattern[0])[4] / (1.0 * patternSeen.get(pattern[0])[0]));
				output += " Min Turn:" + patternSeen.get(pattern[0])[1];
				output += " Max Turn:" + patternSeen.get(pattern[0])[2];
				output += " Ave Turn:" + (patternSeen.get(pattern[0])[3] / (1.0 * patternSeen.get(pattern[0])[0]));
				output += " " + DynamicPattern.longToPatternString(pattern[0], PATTERN_LENGTH) + "\n";
				bw.write(output);
			}
			System.out.println("Done.");
			bw.write(output);
			System.out.println("Written to file "+TEST_DIRECTORY + "output"+PATTERN_LENGTH+".txt");
			bw.close();
			ObjectOutputStream ow = new ObjectOutputStream(new FileOutputStream(new File(TEST_DIRECTORY + "pattern"+PATTERN_LENGTH+".dat")));
			for (Long[] pattern : initialPatternSeen) {
				ow.writeObject(new DynamicPattern(pattern[0], PATTERN_LENGTH));
			}
			ow.flush();
			ow.close();
			System.out.println("Written to file "+TEST_DIRECTORY + "pattern"+PATTERN_LENGTH+".dat");
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
	public Long[][] sortHashMapIntoArray() {
		Set<Long> patterns = patternSeen.keySet();
		Long[][] initialPatternSeen = new Long[patterns.size()][3];
		int index = 0; 
		for(Long pattern : patterns) {
			initialPatternSeen[index][0] = pattern;
			initialPatternSeen[index][1] = patternSeen.get(pattern)[0];
			initialPatternSeen[index][2] = patternSeen.get(pattern)[4];
			index++;
		}
		for (int j = 0; j < initialPatternSeen.length - 1; j++) {
			int maxindex = j+1;
			double maxvalue = (initialPatternSeen[maxindex][2] / (1.0 * initialPatternSeen[maxindex][1]));
			for (int i = j+1; i < initialPatternSeen.length; i++) {
				if ((initialPatternSeen[i][2] / (1.0 * initialPatternSeen[i][1])) > maxvalue) {
					maxindex = i;
					maxvalue = (initialPatternSeen[maxindex][2] / (1.0 * initialPatternSeen[maxindex][1]));
				}
			}
			Long[] swapValue = initialPatternSeen[maxindex];
			initialPatternSeen[maxindex] = initialPatternSeen[j];
			initialPatternSeen[j] = swapValue;
		}
		return initialPatternSeen;
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
					clearOutNonsense();
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
	public void clearOutNonsense() {
		if (patternSeen.size() > PATTERN_STORAGE_CUTOFF) {
			Long[][] data = sortHashMapIntoArray();
			for (int i = PATTERNS_TO_REMOVE; i < data.length; i++) {
				patternSeen.remove(data[i][0]);
			}
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
			int currentTurn = 0;
			Board patternBoard = new Board();
			patternBoard.play(board.getMove(0));
			while (currentTurn <= turn) {
				int currentPlay = board.getMove(currentTurn);
				int lastPlay = board.getMove(currentTurn - 1);
				if (ON_BOARD[lastPlay] && ON_BOARD[currentPlay]) {
					for (int p : ALL_POINTS_ON_BOARD) {
						DynamicPattern pattern = new DynamicPattern(p, patternBoard, PATTERN_LENGTH);
						boolean foundPattern = false;
						for (int i = 0; i < DynamicPattern.NUMBER_CHOICES; i++) {
							if (patternSeen.containsKey(pattern.getPattern()[i])) {
								foundPattern = true;
								Long[] patternData = patternSeen.get(pattern.getPattern()[i]);
								patternData[0] += 1;
								if (currentTurn < patternData[1]) {
									patternData[1] = (long)currentTurn;
								}
								if (currentTurn > patternData[2]) {
									patternData[2] = (long)currentTurn;
								}
								patternData[3] += currentTurn;
								if (p == currentPlay) {
									patternData[4]++;
								}
								patternSeen.put(pattern.getPattern()[i], patternData);
							}
						}
						if (!foundPattern) {
							Long[] patternData = new Long[5];
							patternData[0] = (long)1;
							patternData[1] = (long)currentTurn;
							patternData[2] = (long)currentTurn;
							patternData[3] = (long)currentTurn;
							patternData[4] = (p == currentPlay) ? 1L: 0L;
							patternSeen.put(pattern.getPattern()[0], patternData);
						}
					}
				}
				currentTurn++;
				patternBoard.play(lastPlay);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
