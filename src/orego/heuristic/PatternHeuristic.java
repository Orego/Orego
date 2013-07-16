package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.getNeighbors;
import static orego.core.Coordinates.isOnBoard;
import static orego.core.Coordinates.getAllPointsOnBoard;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.core.Board;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Pattern;
import orego.play.UnknownPropertyException;
import orego.util.BitVector;
import static orego.heuristic.PatternHeuristicPatterns.*;

/** Suggests moves near the last move that match patterns. */
public class PatternHeuristic extends Heuristic {
	

	private int numberOfBadPatterns;
	
	/** Number of good patterns. */
	private int numberOfGoodPatterns;
	/** Empirically, we've determined that this many top patterns should be considered "good". */
	private static int DEFAULT_NUMBER_OF_GOOD_PATTERNS = 400;
		
	/** Good neighborhoods patterns, indexed by color. */
	private BitVector[] goodNeighborhoods = {
											new BitVector(NUMBER_OF_NEIGHBORHOODS),
											new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	protected BitVector getGoodNeighborhoods(int color){
		return goodNeighborhoods[color];
	}
	
	protected int getNumberOfBadPatterns(){
		return numberOfBadPatterns;
	}
	
	protected int getNumberOfGoodPatterns(){
		return numberOfGoodPatterns;
	}
		

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;
	
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
				diagramToNeighborhood("..*\n. *\n***")
	};
		
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
	
	public PatternHeuristic(int weight) {
		super(weight);
		resizeNumberOfGoodPatterns(Math.min(ALL_GOOD_PATTERNS.length, DEFAULT_NUMBER_OF_GOOD_PATTERNS));
	}
		
	@Override
	public PatternHeuristic clone() {
		PatternHeuristic copy = (PatternHeuristic) super.clone();
		// We make a shallow copy of the patterns since we only change them once
		// during Player initialization
		return copy;
	}
	
	@Override
	public void prepare(Board board, boolean local) {
		super.prepare(board, local);
		if (local) {
			int lastMove = board.getMove(board.getTurn() - 1);
			if (!isOnBoard(lastMove)) {
				return;
			}
			for (int p : getNeighbors(lastMove)) {
				if (board.getColor(p) == VACANT) {
					char neighborhood = board.getNeighborhood(p);
					if(goodNeighborhoods[board.getColorToPlay()].get(neighborhood)) {
						recommend(p);
					}
				}
			}
		} else {
			for (int p : getAllPointsOnBoard()) {
				if (board.getColor(p) == VACANT) {
					char neighborhood = board.getNeighborhood(p);
					if(goodNeighborhoods[board.getColorToPlay()].get(neighborhood)) {
						recommend(p);
					}
				}
			}
		}
	}
	
	/** Loops through the potentially good neighborhoods and sets each entry to false. */
	protected void resetGoodPatterns() {
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			goodNeighborhoods[BLACK].set(i, false);
			goodNeighborhoods[WHITE].set(i, false);
		}
	}
	
	/** Resizes the number of good patterns we examine. */
	public void resizeNumberOfGoodPatterns(int newLength) {
		numberOfGoodPatterns = newLength;
		resetGoodPatterns();
		Pattern[] BLACK_GOOD_PATTERNS = new Pattern[numberOfGoodPatterns];
		Pattern[] WHITE_GOOD_PATTERNS = new Pattern[numberOfGoodPatterns];
		// We start at the bottom of the good list and work our way upwards exactly numberOfGoodPattern times
		// since the best patterns are at the bottom of the good list
		int startIndex = (ALL_GOOD_PATTERNS.length - numberOfGoodPatterns);
		int endIndex = ALL_GOOD_PATTERNS.length - 1;
		for (int i = startIndex; i <= endIndex; i++) {
			// we must subtract start index so that we index relative to the smaller arrays
			BLACK_GOOD_PATTERNS[i - startIndex] = new ColorSpecificPattern(ALL_GOOD_PATTERNS[i], BLACK);
			WHITE_GOOD_PATTERNS[i - startIndex] = new ColorSpecificPattern(ALL_GOOD_PATTERNS[i], WHITE);
		}
		// Find all good neighborhoods, i.e., neighborhoods where a player
		// should play.
		// Note that i has to be an int, rather than a char, because
		// otherwise incrementing it after Character.MAX_VALUE would
		// return it to 0, resulting in an infinite loop.
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			if (!isPossibleNeighborhood((char) i)) {
				continue;
			}
			for (Pattern pattern : BLACK_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					goodNeighborhoods[BLACK].set(i, true);
				}
			}
			for (Pattern pattern : WHITE_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					goodNeighborhoods[WHITE].set(i, true);
				}
			}
		}
		
	}
		
	@Override
	public void setProperty(String name, String value) throws UnknownPropertyException {
		if (name.equals("numberOfGoodPatterns")) {
			resizeNumberOfGoodPatterns(Integer.valueOf(value));
		} else {
			super.setProperty(name, value);
		}		
	}

}
