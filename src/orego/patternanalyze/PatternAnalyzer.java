package orego.patternanalyze;

import static orego.core.Board.NINE_PATTERN;
import static orego.core.Board.THREE_PATTERN;
import static orego.core.Colors.VACANT;
import static orego.mcts.PatternPlayer.NUM_HASH_TABLES;
import static orego.mcts.PatternPlayer.hashLongToChar;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import orego.core.Board;
import orego.core.Colors;
import orego.shape.PatternInformation;

@SuppressWarnings("unchecked")
public class PatternAnalyzer {

	@SuppressWarnings("unchecked")
	private static final HashMap<Character, PatternInformation>[][][] PATTERNS = new HashMap[NUM_HASH_TABLES][NINE_PATTERN + 1][2];

	static { // load hash maps
		for (int c = 0; c < 2; c++) {
			for (int i = 0; i < NINE_PATTERN + 1; i++) {
				for (int table = 0; table < NUM_HASH_TABLES; table++) {
					// load from files
					try {
						ObjectInputStream ir = new ObjectInputStream(
								new FileInputStream(new File(
										"./testFiles/patternPlayed"
												+ (i * 2 + 3)
												+ Colors.colorToString(c)
												+ table + ".dat")));
						PATTERNS[table][i][c] = (HashMap<Character, PatternInformation>) (ir
								.readObject());
						ir.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private static class PatternInfo implements Comparable<PatternInfo> {
		private String pattern;
		private double winRate;
		private int colorToPlay;

		public PatternInfo(String pattern, double winRate, int colorToPlay) {
			this.pattern = pattern;
			this.winRate = winRate;
		}

		public int compareTo(PatternInfo other) {
			double value = other.winRate - winRate;
			if (value > 0)
				return (int) Math.ceil(value);
			else
				return (int) Math.floor(value);
		}

		public String toString() {
			return Colors.colorToChar(colorToPlay) + " " + winRate + "\n"
					+ pattern;
		}
	}

	public static PatternInformation[] getInformation(int patternType,
			long hash, int color) {
		PatternInformation[] toReturn = new PatternInformation[NUM_HASH_TABLES];
		for (int table = 0; table < NUM_HASH_TABLES; table++) {
			toReturn[table] = PATTERNS[table][patternType][color]
					.get(hashLongToChar(hash, table));
			if (toReturn[table] == null)
				toReturn[table] = new PatternInformation();
		}
		return toReturn;
	}

	protected static List<PatternInfo> getHighestAndLowest3x3s(
			double threshold, boolean collectBothHighAndLow) {
		LinkedList<PatternInfo> pats = new LinkedList<PatternInfo>();
		for (int pattern = 0; pattern < Character.MAX_VALUE; pattern++) {
			if (isValid3x3Pattern((char) pattern)) {
				for (int color = 0; color < 2; color++) {
					PatternInformation[] infos = getInformation(THREE_PATTERN,
							charPatternTo3x3Hash((char) pattern), color);
					double rate = 0;
					long runs = 0;
					for (PatternInformation i : infos) {
						rate += i.getRate();
						runs += i.getRuns();
					}
					rate /= 4.0;
					runs /= 4;
					if (runs > 1
							&& (rate + threshold > 1 || (rate < threshold && collectBothHighAndLow))) {
						pats.add(new PatternInfo(
								charTo3x3Pattern((char) pattern), rate, color));
					}
				}
			}
		}
		Collections.sort(pats);
		return pats;
	}

	private static String charTo3x3Pattern(char pattern) {
		String toReturn = "";
		for (int i = 0; i < 8; i++) {
			toReturn += Colors.colorToChar((pattern & 0xc000) >>> 14);
			pattern = (char) (pattern << 2);
			if (i == 3)
				toReturn += Colors.colorToChar(VACANT);
			if (i == 2 || i == 4)
				toReturn += "\n";
		}
		toReturn += "\n";
		return toReturn;
	}

	private static long charPatternTo3x3Hash(char pattern) {
		long hash = 0;
		for (int i = 0; i < 8; i++) {
			hash ^= Board.ZOBRIST_PATTERNS[THREE_PATTERN][(pattern & 0xc000) >>> 14][i];
			pattern = (char) (pattern << 2);
		}

		return hash;
	}

	private static boolean isValid3x3Pattern(char pattern) {
		int numEdges = 0;
		boolean validNW = true, validNE = true, validSW = true, validSE = true;
		for (int i = 0; i < 8; i++) {
			if (Colors.OFF_BOARD_COLOR == (pattern & 0xc000) >>> 14) {
				numEdges++;
				switch (i) {
				case 1:
					validSW = false;
				case 0:
					validSE = false;
					break;
				case 4:
					validNW = false;
				case 2:
					validSW = false;
					break;
				case 3:
					validNE = false;
					validSE = false;
					break;
				case 6:
					validNW = false;
				case 5:
					validNE = false;
					break;
				case 7:
					validNW = false;
				}
			}
			pattern = (char) (pattern << 2);
		}
		if (numEdges == 0) {
			return true;
		} else if (numEdges != 5) {
			return false;
		} else {
			return validNE || validNW || validSW || validSE;
		}
	}
	
//	public static void main(String[] args) {
//		Scanner scan = new Scanner(System.in);
//		String pattern;
//		System.out.println("Input pattern all on one line:");
//		do {
//			pattern = scan.nextLine();
//			int offset = 0;
//			long hash = 0;
//			for (int i =0; i<8; i++){
//				hash ^= Board.ZOBRIST_PATTERNS[THREE_PATTERN][color][index];
//				
//				
//				if (i==3)
//					offset = 1;
//			}
//		}while(true);
//	}
}
