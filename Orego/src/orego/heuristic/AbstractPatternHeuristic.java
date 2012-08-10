package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Cut1Pattern;
import orego.patterns.Pattern;
import orego.patterns.SimplePattern;
import orego.util.BitVector;

public abstract class AbstractPatternHeuristic extends Heuristic {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;
	
	public static final BitVector[] BAD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
	public static final BitVector[] GOOD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
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

	/**
	 * Set of 3x3 patterns taken from Gelly et al,
	 * "Modification of UCT with Patterns in Monte-Carlo Go"
	 * 
	 * @see orego.core.Coordinates#NEIGHBORS
	 */
	static {
		String[] colorSpecificPatterns = {
				"O...#O??", // Hane4
				"#??*?O**", // Edge3
				"O?+*?#**", // Edge4
				"O#O*?#**", // Edge5	
		};
		Pattern[] BLACK_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		Pattern[] WHITE_GOOD_PATTERNS = new Pattern[colorSpecificPatterns.length];
		for (int i = 0; i < BLACK_GOOD_PATTERNS.length; i++) {
			BLACK_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], BLACK);
			WHITE_GOOD_PATTERNS[i] = new ColorSpecificPattern(colorSpecificPatterns[i], WHITE);
		}
		String[] colorIndependentPatterns = {
				"O..?##??", // Hane1
				"O...#.??", // Hane2
				"O#..#???", // Hane3
				"#OO+??++", // Cut2
				".O?*#?**", // Edge1
				"#oO*??**", // Edge2				
		};
		Pattern[] INDEPENDENT_GOOD_PATTERNS = new Pattern[colorIndependentPatterns.length + 1];			
		for (int i = 0; i < colorIndependentPatterns.length; i++) {
			INDEPENDENT_GOOD_PATTERNS[i] = new SimplePattern(colorIndependentPatterns[i]);
		}
		INDEPENDENT_GOOD_PATTERNS[INDEPENDENT_GOOD_PATTERNS.length - 1] = new Cut1Pattern();
		String[] badPatterns = {
				"O.OO?oo?", // Tiger's mouth 
				".#..#.?.", // Empty triangle
				".OO?OO??", // Push through bamboo
		};
		Pattern[] BLACK_BAD_PATTERNS = new Pattern[badPatterns.length];
		Pattern[] WHITE_BAD_PATTERNS = new Pattern[badPatterns.length];
		for (int i = 0; i < BLACK_BAD_PATTERNS.length; i++) {
			BLACK_BAD_PATTERNS[i] = new ColorSpecificPattern(badPatterns[i], BLACK);
			WHITE_BAD_PATTERNS[i] = new ColorSpecificPattern(badPatterns[i], WHITE);
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
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (Pattern pattern : WHITE_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			for (Pattern pattern : INDEPENDENT_GOOD_PATTERNS) {
				if (pattern.matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			for (Pattern pattern : BLACK_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					BAD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (Pattern pattern : WHITE_BAD_PATTERNS) {
				if (pattern.matches((char) i)) {
					BAD_NEIGHBORHOODS[WHITE].set(i, true);
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

	public AbstractPatternHeuristic(int weight) {
		super(weight);
	}

}
