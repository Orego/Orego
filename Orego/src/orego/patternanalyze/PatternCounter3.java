package orego.patternanalyze;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;


/**
 * This class uses all the points on the board when looking for patterns.
 * A pattern is "Seen" if it was on the board at any point.
 * A pattern is "Played" if it was played at any point.
 * These are sorted based on the ratio of patterns seen to patterns played.
 * @author galbraith
 *
 */

public class PatternCounter3 {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;

	private static final int PATTERN_LENGTH = 8;
	
	private static final int PATTERN_STORAGE_CUTOFF = 500000;
	private static final int PATTERNS_TO_REMOVE = PATTERN_STORAGE_CUTOFF / 2;
	
	private static final int PATTERNS_TO_KEEP = 3000;
	
	private static final int PATTERN_SEEN = 0;
	private static final int MIN_TURN = 1;
	private static final int MAX_TURN = 2;
	private static final int TOTAL_TURN = 3;
	private static final int PATTERN_PLAYED = 4;
	
	private static final int SORTED_ARRAY_PATTERN = 0;
	private static final int SORTED_ARRAY_SEEN = 1;
	private static final int SORTED_ARRAY_PLAYED = 2;
	
	private static final boolean DEBUG = false;

	/**
	 * Long[] will have:
	 * 0: frequency seen
	 * 1: largest turn
	 * 2: lowest turn
	 * 3: total of all turns
	 * */
	private static HashMap<Long, Long[]> patternSeen = new HashMap<Long, Long[]>();
	private static String TEST_DIRECTORY = "../../../Test Games/";

	public static void main(String[] args) {
		new PatternCounter3();
	}

	public PatternCounter3() {
		try {
			setUp(TEST_DIRECTORY);
			int threshold = 1;
			while (patternSeen.size() > PATTERNS_TO_KEEP) {
				removePatterns(-1, threshold++);
			}
			PrintWriter bw = new PrintWriter(new FileWriter(new File(
					TEST_DIRECTORY + "outputRatio"+PATTERN_LENGTH+".txt")));
			String output = "";
			Long[][] initialPatternSeen = sortHashMapIntoArray();
			for (Long[] pattern : initialPatternSeen) {
				output = "Seen:" + patternSeen.get(pattern[SORTED_ARRAY_PATTERN])[PATTERN_SEEN];
				output += " Played:" + patternSeen.get(pattern[SORTED_ARRAY_PATTERN])[PATTERN_PLAYED];
				output += " Ratio:" +  (patternSeen.get(pattern[SORTED_ARRAY_PATTERN])[PATTERN_PLAYED] / (1.0 * patternSeen.get(pattern[SORTED_ARRAY_PATTERN])[PATTERN_SEEN]));
				output += " Min Turn:" + patternSeen.get(pattern[SORTED_ARRAY_PATTERN])[MIN_TURN];
				output += " Max Turn:" + patternSeen.get(pattern[SORTED_ARRAY_PATTERN])[MAX_TURN];
				output += " Ave Turn:" + (patternSeen.get(pattern[SORTED_ARRAY_PATTERN])[TOTAL_TURN] / (1.0 * patternSeen.get(pattern[SORTED_ARRAY_PATTERN])[PATTERN_SEEN]));
				output += " " + DynamicPattern.longToPatternString(pattern[SORTED_ARRAY_PATTERN], PATTERN_LENGTH) + "\n";
				bw.write(output);
			}
			System.out.println("Done.");
			bw.write(output);
			System.out.println("Written to file "+TEST_DIRECTORY + "outputRatio"+PATTERN_LENGTH+".txt");
			bw.close();
			ObjectOutputStream ow = new ObjectOutputStream(new FileOutputStream(new File(TEST_DIRECTORY + "patternRatio"+PATTERN_LENGTH+".dat")));
			for (Long[] pattern : initialPatternSeen) {
				ow.writeObject(new DynamicPattern(pattern[0], PATTERN_LENGTH));
			}
			ow.flush();
			ow.close();
			System.out.println("Written to file "+TEST_DIRECTORY + "patternRatio"+PATTERN_LENGTH+".dat");
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
		System.out.println("Sorting array...");
		Set<Long> patterns = patternSeen.keySet();
		Long[][] sortedArray = new Long[patterns.size()][3];
		int index = 0; 
		for(Long pattern : patterns) {
			sortedArray[index][SORTED_ARRAY_PATTERN] = pattern;
			sortedArray[index][SORTED_ARRAY_SEEN] = patternSeen.get(pattern)[PATTERN_SEEN];
			sortedArray[index][SORTED_ARRAY_PLAYED] = patternSeen.get(pattern)[PATTERN_PLAYED];
			index++;
		}
		for (int j = 0; j < sortedArray.length - 1; j++) {
			int maxindex = j+1;
			double maxvalue = (sortedArray[maxindex][SORTED_ARRAY_PLAYED] / (1.0 * sortedArray[maxindex][SORTED_ARRAY_SEEN]));
			for (int i = j+1; i < sortedArray.length; i++) {
				if ((sortedArray[i][SORTED_ARRAY_PLAYED] / (1.0 * sortedArray[i][SORTED_ARRAY_SEEN])) > maxvalue) {
					maxindex = i;
					maxvalue = (sortedArray[maxindex][SORTED_ARRAY_PLAYED] / (1.0 * sortedArray[maxindex][SORTED_ARRAY_SEEN]));
				}
			}
			Long[] swapValue = sortedArray[maxindex];
			sortedArray[maxindex] = sortedArray[j];
			sortedArray[j] = swapValue;
		}
		System.out.println("Done sorting array");
		return sortedArray;
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
					if (DEBUG) System.out.println("Number of patterns:" + patternSeen.size() + " Game:" + i);
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
			int threshold = 1;
			System.out.println("Removing patterns...");
			while (patternSeen.size() > PATTERNS_TO_REMOVE) {
				removePatterns(patternSeen.size() - PATTERNS_TO_REMOVE, threshold++);
			}
			System.out.println("Done removing patterns");
		}
	}

	/**
	 * Remove patterns seen only once.  If toRemove is -1 it will remove all patterns seen once.
	 * @param Number of patterns to remove.
	 * @return
	 */
	private int removePatterns(int toRemove, int threshold) {
		int removed = 0;
		Set<Long> patterns = patternSeen.keySet();
		Object[] keys = patterns.toArray();
		for(Object key : keys) {
			Long pattern = (Long)key;
			if (patternSeen.get(pattern)[PATTERN_SEEN] <= threshold) {
				patternSeen.remove(pattern);
				removed++;
				if (removed >= toRemove && toRemove != -1){
					break;
				}
			}
		}
		if (DEBUG) System.out.println("Removed single patterns:" + removed + " of size:"+threshold);
		return removed;
	}

	
	/** 
	 * Check for the patterns in a particular file.
	 * @param SGF file of a particular game.
	 */
	public void checkPatterns(File dir) {
		try {
			System.out.println("Starting file "+dir.getName());
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
			ArrayList<Long> patternSeenInGame = new ArrayList<Long>();
			ArrayList<Long> patternPlayedInGame = new ArrayList<Long>();
			while (currentTurn <= turn) {
				int currentPlay = board.getMove(currentTurn);
				int lastPlay = board.getMove(currentTurn - 1);
				if (ON_BOARD[lastPlay] && ON_BOARD[currentPlay]) {
					for (int p : ALL_POINTS_ON_BOARD) {
						if (patternBoard.getColor(p) == VACANT) {
							DynamicPattern pattern = new DynamicPattern(p, patternBoard, PATTERN_LENGTH);
							boolean foundPattern = false;
							for (int i = 0; i < DynamicPattern.NUMBER_CHOICES; i++) {
								if (patternSeen.containsKey(pattern.getPattern()[i])) {
									foundPattern = true;
									Long[] patternData = patternSeen.get(pattern.getPattern()[i]);
									if (!patternSeenInGame.contains(pattern.getPattern()[i])) {
										patternData[PATTERN_SEEN] += 1;
										patternSeenInGame.add(pattern.getPattern()[i]);
									}
									if (currentTurn < patternData[MIN_TURN]) {
										patternData[MIN_TURN] = (long)currentTurn;
									}
									if (currentTurn > patternData[MAX_TURN]) {
										patternData[MAX_TURN] = (long)currentTurn;
									}
									patternData[TOTAL_TURN] += currentTurn;
									patternSeen.put(pattern.getPattern()[i], patternData);
								}
								if (patternSeen.containsKey(pattern.getPattern()[i]) && (p == currentPlay) &&
										(!patternPlayedInGame.contains(pattern.getPattern()[i]))) {
									patternSeen.get(pattern.getPattern()[i])[PATTERN_PLAYED]++;
									patternPlayedInGame.add(pattern.getPattern()[i]);
								}
							}
							if (!foundPattern) {
								Long[] patternData = new Long[5];
								patternData[PATTERN_SEEN] = (long)1;
								patternSeenInGame.add(pattern.getPattern()[0]);
								patternData[MIN_TURN] = (long)currentTurn;
								patternData[MAX_TURN] = (long)currentTurn;
								patternData[TOTAL_TURN] = (long)currentTurn;
								patternData[PATTERN_PLAYED] = 0L;
								patternSeen.put(pattern.getPattern()[0], patternData);
								if ((p == currentPlay) && (!patternPlayedInGame.contains(pattern.getPattern()[0]))) {
									patternSeen.get(pattern.getPattern()[0])[PATTERN_PLAYED]++;
									patternPlayedInGame.add(pattern.getPattern()[0]);
								}
							}
						}
					}
				}
				currentTurn++;
				patternBoard.play(lastPlay);
			}
			System.out.println("Finished file "+dir.getName());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
