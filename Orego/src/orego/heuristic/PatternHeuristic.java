package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.core.Coordinates.ON_BOARD;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.core.Board;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Pattern;
import orego.play.UnknownPropertyException;
import orego.util.BitVector;

public class PatternHeuristic extends Heuristic {
	
	protected int numberOfBadPatterns;
	
	protected int numberOfGoodPatterns;
	
	protected BitVector[] goodNeighborhoods = {
											new BitVector(NUMBER_OF_NEIGHBORHOODS),
											new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	protected BitVector[] badNeighborhoods  = {
											new BitVector(NUMBER_OF_NEIGHBORHOODS),
											new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;
	
	public PatternHeuristic(int weight) {
		super(weight);
		
		// TODO: these should be set to an appropriate initial value
		numberOfGoodPatterns = 400;
		numberOfBadPatterns  = 3;
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
				diagramToNeighborhood("..*\n. *\n***")

	};
	
	/** Loops through the potentially good neighborhoods and sets each entry to false*/
	protected void resetGoodPatterns() {
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			goodNeighborhoods[BLACK].set(i, false);
			goodNeighborhoods[WHITE].set(i, false);
		}
	}
	
	/** Loops through the potentially bad neighborhoods and sets each entry to false*/
	protected void resetBadPatterns() {
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			badNeighborhoods[BLACK].set(i, false);
			badNeighborhoods[WHITE].set(i, false);
		}
	}
	
	/** Resizes the number of good patterns we examine. */
	public void resizeNumberOfGoodPatterns(int newLength) {
		numberOfGoodPatterns = newLength;
		
		resetGoodPatterns();
		
		Pattern[] BLACK_GOOD_PATTERNS = new Pattern[numberOfGoodPatterns];
		Pattern[] WHITE_GOOD_PATTERNS = new Pattern[numberOfGoodPatterns];
		
		// we start at the bottom of the good list and work our way upwards
		// since the best patterns are at the bottom of the good list
		for (int i = numberOfGoodPatterns - 1; i >= 0; i++) {
			BLACK_GOOD_PATTERNS[i] = new ColorSpecificPattern(PatternHeuristicPatterns.ALL_GOOD_PATTERNS[i], BLACK);
			WHITE_GOOD_PATTERNS[i] = new ColorSpecificPattern(PatternHeuristicPatterns.ALL_GOOD_PATTERNS[i], WHITE);
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
	
	/** Resizes the number of bad patterns we examine. */
	public void resizeNumberOfBadPatterns(int newLength) {
		numberOfBadPatterns = newLength;
		
		resetBadPatterns();
		
		Pattern[] BLACK_BAD_PATTERNS = new Pattern[numberOfBadPatterns];
		Pattern[] WHITE_BAD_PATTERNS = new Pattern[numberOfBadPatterns];
		
		// we start at the top and work our way down since the "worst" bad patterns are at the top
		// of the list
		for (int i = 0; i < numberOfBadPatterns; i++) {
			BLACK_BAD_PATTERNS[i] = new ColorSpecificPattern(PatternHeuristicPatterns.ALL_BAD_PATTERNS[i], BLACK);
			WHITE_BAD_PATTERNS[i] = new ColorSpecificPattern(PatternHeuristicPatterns.ALL_BAD_PATTERNS[i], WHITE);
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
			
			for (Pattern pattern : BLACK_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					badNeighborhoods[BLACK].set(i, true);
				}
			}
			for (Pattern pattern : WHITE_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					badNeighborhoods[WHITE].set(i, true);
				}
			}	
		}
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
		int lastMove = board.getMove(board.getTurn() - 1);
		if (!ON_BOARD[lastMove]) {
			return;
		}
		for (int p : NEIGHBORS[lastMove]) {
			if (board.getColor(p) == VACANT) {
				char neighborhood = board.getNeighborhood(p);
				if(goodNeighborhoods[board.getColorToPlay()].get(neighborhood)) {
					recommend(p);
				}
			}
		}
	}
	
	@Override
	public PatternHeuristic clone() {
		PatternHeuristic copy = (PatternHeuristic) super.clone();
		
		// we make a shallow copy of the patterns since we only change them once
		// during Player initialization
		copy.badNeighborhoods     = this.badNeighborhoods;
		copy.goodNeighborhoods    = this.goodNeighborhoods;
		copy.numberOfBadPatterns  = this.numberOfBadPatterns;
		copy.numberOfGoodPatterns = this.numberOfGoodPatterns;
		return copy;
	}
	
	public boolean isBad(int p, Board board) {
		return badNeighborhoods[board.getColorToPlay()].get(board.getNeighborhood(p));
	}
	
	@Override
	public void setProperty(String name, String value) throws UnknownPropertyException {
		if (name.equals("numberOfBadPatterns")) {
			resizeNumberOfBadPatterns(Integer.valueOf(value));
		} else if (name.equals("numberOfGoodPatterns")) {
			resizeNumberOfGoodPatterns(Integer.valueOf(value));
		} else {
			super.setProperty(name, value);
		}
		
	}

}
