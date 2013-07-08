package orego.patternanalyze;

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
import orego.util.IntSet;


/**
 * This class looks only at patterns that were actually played.
 * These are sorted based on the ratio of patterns seen to patterns played.
 * @author galbraith
 */


public class PatternCounterTwoPointOne {

	/**
	 * The number of total patterns, including impossible ones.
	 */
//	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;

//	private static final int PATTERN_LENGTH = 24;
	
//	private static final int PATTERN_STORAGE_CUTOFF = 500000;
//	private static final int PATTERNS_TO_REMOVE = PATTERN_STORAGE_CUTOFF / 2;
	
//	private static final int PATTERNS_TO_KEEP = (int)Math.pow(2, 16);
	
//	private static final int SORTED_ARRAY_PATTERN = 0;
//	private static final int SORTED_ARRAY_PLAYED = 1;
	
	private static final boolean DEBUG = false;

	/**
	 * Long[] will have:
	 * 0: frequency seen
	 * 1: largest turn
	 * 2: lowest turn
	 * 3: total of all turns
	 * */
	private static HashMap<Character, PatternInformation>[] patternSeen;
	private static String TEST_DIRECTORY = "../../Test Games/kgs-19-2001/";
	//private static String TEST_DIRECTORY = "../../Test Games/";

	public static void main(String[] args) {
		new PatternCounterTwoPointOne();
	}

	@SuppressWarnings("unchecked")
	public PatternCounterTwoPointOne() {
		try {
			patternSeen = new HashMap[4];
			for (int i = 0; i < NINE_PATTERN+1; i++) {
				patternSeen[i] = new HashMap<Character, PatternInformation>();
			}
			setUp(TEST_DIRECTORY);
			PrintWriter bw = new PrintWriter(new FileWriter(new File(
					TEST_DIRECTORY + "outputPlayed.txt")));
			String output = "";
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				Set<Character> initialPatternSeen = patternSeen[i].keySet();
				output += "Pattern:" + (i*2+3) + " below.\n";
				bw.write(output);
				System.out.println("Total patterns:" + initialPatternSeen.size());
				for (Character pattern : initialPatternSeen) {		
					if (patternSeen[i].get(pattern) != null) {
						output = " Total Wins:" + patternSeen[i].get(pattern).getRate() * patternSeen[i].get(pattern).getRuns();
						output += " Total Runs:" + patternSeen[i].get(pattern).getRuns();
						output += " Win Rate:" + patternSeen[i].get(pattern).getRate();
						output += " "+(i*2+3)+"\n";
						output += patternSeen[i].get(pattern).toString();
						bw.write(output);
					}
				}
			}
			System.out.println("Done.");
			bw.write(output);
			System.out.println("Written to file "+TEST_DIRECTORY + "outputPlayed.txt");
			bw.close();
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				ObjectOutputStream ow = new ObjectOutputStream(new FileOutputStream(new File(TEST_DIRECTORY + "patternPlayed"+(i*2+3)+".dat")));
				ow.writeObject(patternSeen[i]);
				ow.flush();
				ow.close();
				System.out.println("Written to file "+TEST_DIRECTORY + "patternPlayed"+(i*2+3)+".dat");
			}
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
	/*public PatternInformation[] sortHashMapIntoArray() {
		PatternInformation[] toReturn = new PatternInformation[NINE_PATTERN + 1];
		for (int i = 0; i < NINE_PATTERN + 1; i++) {		
			Set<Character> patterns = patternSeen[i].keySet();
			PatternInformation[] sortedArray = new PatternInformation[patterns.size()];
			int index = 0; 
			for(char pattern : patterns) {
				sortedArray[index] = patternSeen[i].get(pattern);
				index++;
			}
			for (int j = 0; j < sortedArray.length - 1; j++) {
				int maxindex = j+1;
				double maxvalue = (sortedArray[j].getRuns());
				for (int k = j+1; k < sortedArray.length; k++) {
					if (sortedArray[k].getRuns() > maxvalue) {
						maxindex = k;
						maxvalue = sortedArray[k].getRuns();
					}
				}
				PatternInformation swapValue = sortedArray[maxindex];
				sortedArray[maxindex] = sortedArray[j];
				sortedArray[j] = swapValue;
			}
			toReturn = sortedArray;
		}
		return toReturn;
	}*/

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
					//if (DEBUG) System.out.println("Number of patterns:" + patternSeen.size() + " Game:" + i);
					//clearOutNonsense();
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
	/*public void clearOutNonsense() {
		if (patternSeen.size() > PATTERN_STORAGE_CUTOFF) {
			int threshold = 1;
			while (patternSeen.size() > PATTERNS_TO_REMOVE) {
				removePatterns(patternSeen.size() - PATTERNS_TO_REMOVE, threshold++);
			}
		}
	}*/

	/**
	 * Remove patterns seen only once.  If toRemove is -1 it will remove all patterns seen once.
	 * @param Number of patterns to remove.
	 * @return
	 */
	/*private int removePatterns(int toRemove, int threshold) {
		int removed = 0;
		Set<Character> patterns = patternSeen.keySet();
		Object[] keys = patterns.toArray();
		for(Object key : keys) {
			Long pattern = (Long)key;
			if (patternSeen.get(pattern)[PATTERN_PLAYED] <= threshold) {
				patternSeen.remove(pattern);
				removed++;
				if (removed >= toRemove && toRemove != -1){
					break;
				}
			}
		}
		if (DEBUG) System.out.println("Removed single patterns:" + removed + " of size:"+threshold);
		return removed;
	}*/

	
	/** 
	 * Check for the patterns in a particular file.
	 * @param SGF file of a particular game.
	 */
	public void checkPatterns(File dir) {
		try {
			System.out.println(dir.getAbsolutePath());
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
			IntSet possibleMoves;
			int randomMove;
			Board patternBoard = new Board();
			while (currentTurn <= turn) {
				int nextPlay = board.getMove(currentTurn);
				do {
					possibleMoves = patternBoard.getVacantPoints();
					randomMove = possibleMoves.get((int)(Math.random()*possibleMoves.size()));
				} while (!patternBoard.isLegal(randomMove) || randomMove == nextPlay);
				if (isOnBoard(nextPlay)) {
					for (int patternType = 0; patternType < NINE_PATTERN + 1; patternType++) {
						char lastPlayHash = patternBoard.getPatternHash(patternType, nextPlay);
						if (patternSeen[patternType].containsKey(lastPlayHash)) {
							PatternInformation patternData = patternSeen[patternType].get(lastPlayHash);
							float totalWins = (patternData.getRate() * patternData.getRuns());
							patternData.setRuns(patternData.getRuns() + 1);
							patternData.setRate((totalWins+1) / patternData.getRuns());
						}
						else {
							patternSeen[patternType].put(lastPlayHash, new PatternInformation(1.0f, 1, patternBoard.printPattern(patternType, nextPlay)));
						}
						char moveHash = patternBoard.getPatternHash(patternType, randomMove);
						if (patternSeen[patternType].containsKey(moveHash)) {
							PatternInformation patternData = patternSeen[patternType].get(moveHash);
							float totalWins = (patternData.getRate() * patternData.getRuns());
							patternData.setRuns(patternData.getRuns() + 1);
							patternData.setRate((totalWins) / patternData.getRuns());
						}
						else {
							patternSeen[patternType].put(moveHash, new PatternInformation(0.0f, 1, patternBoard.printPattern(patternType, randomMove)));
						}
					}
				}
				currentTurn++;
				patternBoard.play(nextPlay);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
