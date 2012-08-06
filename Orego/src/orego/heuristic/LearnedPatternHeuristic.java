package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.experiment.Debug.OREGO_ROOT_DIRECTORY;
import static orego.patterns.Pattern.diagramToNeighborhood;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import orego.core.Board;
import orego.patternanalyze.DynamicPattern;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Pattern;
import orego.util.BitVector;

public class LearnedPatternHeuristic extends Heuristic {

	public LearnedPatternHeuristic(int weight) {
		super(weight);
	}
	
	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;
	
	private static int GOOD_PATTERNS_TO_LOAD = 100;
	private static int BAD_PATTERNS_TO_LOAD = 0;
	
	public static final BitVector[] GOOD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	public static final BitVector[] BAD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	/**
	 * Set of 3x3 patterns learned from KGS games.
	 */
	private static Pattern[] GOOD_PATTERN_LIST_BLACK;
	private static Pattern[] GOOD_PATTERN_LIST_WHITE;
	private static Pattern[] BAD_PATTERN_LIST_BLACK;
	private static Pattern[] BAD_PATTERN_LIST_WHITE;
	
	/**
	 * Extracts patterns from the specified file.
	 * @param fileName
	 * @param patternList
	 */
	private static Pattern[] extractPatternsFromFile(String fileName, int color, boolean goodMoves) {
		Pattern[] returnedPatterns;
		if (goodMoves) {
			returnedPatterns = new Pattern[GOOD_PATTERNS_TO_LOAD];
		}
		else {
			returnedPatterns = new Pattern[BAD_PATTERNS_TO_LOAD];
		}
		int patternIndex = 0;
		ObjectInputStream input;
		try {
			input = new ObjectInputStream(new FileInputStream(
					new File(fileName)));
			DynamicPattern pattern = null;
			ArrayList<Pattern> badPatterns = new ArrayList<Pattern>();
			try {
				if (goodMoves) {
					int counter = 0;
					while ((pattern = (DynamicPattern) input.readObject()) != null && counter < GOOD_PATTERNS_TO_LOAD) {
						if (pattern.getColorToPlay() == color) {
							ColorSpecificPattern p = translatePattern(pattern.getPattern()[0]);
							returnedPatterns[patternIndex] = p;
							patternIndex++;
							counter++;
						}
					}
				}
				else {
					while ((pattern = (DynamicPattern) input.readObject()) != null) {
						if (pattern.getColorToPlay() == color) {
							badPatterns.add(translatePattern(pattern.getPattern()[0]));
						}
					}
				}
				input.close();
			} catch (EOFException ex) {
				input.close();
				for (int i = 0; i < returnedPatterns.length; i++) {
					returnedPatterns[i] = badPatterns.get(badPatterns.size() - 1 - i);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return returnedPatterns;
	}
	
	public static ColorSpecificPattern translatePattern(long input) {
		String incoming = DynamicPattern.longToPatternString(input, 8);
		int color = "#O".indexOf(incoming.charAt(incoming.length() - 1));
		String output = "" + incoming.charAt(0) + incoming.charAt(3)
						+ incoming.charAt(1) + incoming.charAt(2) 
						+ incoming.charAt(4) + incoming.charAt(5)
						+ incoming.charAt(7) + incoming.charAt(6);
		if (color == WHITE){
			output = output.replace('#', '(');
			output = output.replace('O', ')');
			output = output.replace(')', '#');
			output = output.replace('(', 'O');
		}
		return new ColorSpecificPattern(output, color);
	}

	/**
	 * Used by isPossibleNeighborhood().
	 */
	public static final char[] VALID_OFF_BOARD_PATTERNS = {
			diagramToNeighborhood("...\n. .\n..."),
			diagramToNeighborhood("*..\n* .\n*.."),
			diagramToNeighborhood("..*\n. *\n..*"),
			diagramToNeighborhood("***\n. .\n..."),
			diagramToNeighborhood("...\n. .\n***"),
			diagramToNeighborhood("***\n* .\n*.."),
			diagramToNeighborhood("***\n. *\n..*"),
			diagramToNeighborhood("*..\n* .\n***"),
			diagramToNeighborhood("..*\n. *\n***") };

	static {
		// Find all good neighborhoods, i.e., neighborhoods where a player
		// should play.
		// Note that i has to be an int, rather than a char, because
		// otherwise incrementing it after Character.MAX_VALUE would
		// return it to 0, resulting in an infinite loop.
		GOOD_PATTERN_LIST_BLACK = extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern8.dat", BLACK, true);
		GOOD_PATTERN_LIST_WHITE = extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern8.dat", WHITE, true);
		BAD_PATTERN_LIST_BLACK = extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern8.dat", BLACK, false);
		BAD_PATTERN_LIST_WHITE = extractPatternsFromFile(OREGO_ROOT_DIRECTORY+File.separator+"testFiles/pattern8.dat", WHITE, false);	
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			GOOD_PATTERN_LIST_BLACK[0].matches((char) i);
			if (!isPossibleNeighborhood((char) i)) {
				continue;
			}
			for (int p = 0; p < GOOD_PATTERN_LIST_BLACK.length; p++) {
				if (GOOD_PATTERN_LIST_BLACK[p].matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (int p = 0; p < GOOD_PATTERN_LIST_WHITE.length; p++) {
				if (GOOD_PATTERN_LIST_WHITE[p].matches((char) i)) {
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			for (int p = 0; p < BAD_PATTERN_LIST_BLACK.length; p++) {
				if (BAD_PATTERN_LIST_BLACK[p].matches((char) i)) {
					BAD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (int p = 0; p < BAD_PATTERN_LIST_WHITE.length; p++) {
				if (BAD_PATTERN_LIST_WHITE[p].matches((char) i)) {
					BAD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
		}
	}

	public static final int evaluateMove(int color, char neighborhood) {
		if(GOOD_NEIGHBORHOODS[color].get(neighborhood)) {
			return 1;
		}
		if(BAD_NEIGHBORHOODS[color].get(neighborhood)) {
			return -1;
		}
		return 0;
	}
	
	/**
	 * Returns true if the the specified 3x3 neighborhood can possibly occur.
	 * Neighborhoods are impossible if, for example, there are non-contiguous
	 * off-board points.
	 */
	public static boolean isPossibleNeighborhood(char neighborhood) {
		int mask = 0x3;
		// Replace black and white with vacant, leaving only
		// vacant and off-board colors
		for (int i = 0; i < 16; i += 2) {
			if ((neighborhood >>> i & mask) != OFF_BOARD_COLOR) {
				neighborhood &= ~(mask << i);
				neighborhood |= VACANT << i;
			}
		}
		// Verify that the resulting pattern is valid
		assert VALID_OFF_BOARD_PATTERNS != null;
		for (char v : VALID_OFF_BOARD_PATTERNS) {
			if (neighborhood == v) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void prepare(Board board) {
		super.prepare(board);
		for (int p : NEIGHBORS[board.getMove(board.getTurn() - 1)]) {
			if (board.getColor(p) == VACANT) {
				char neighborhood = board.getNeighborhood(p);
				if(GOOD_NEIGHBORHOODS[board.getColorToPlay()].get(neighborhood)) {
					recommend(p);
				}
				if(BAD_NEIGHBORHOODS[board.getColorToPlay()].get(neighborhood)) {
					discourage(p); 
				}
			}
		}
	}

}
