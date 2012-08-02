package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.*;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.core.Board;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Cut1Pattern;
import orego.patterns.Pattern;
import orego.patterns.SimplePattern;
import orego.util.BitVector;

public class PatternHeuristic extends Heuristic {
	
	public PatternHeuristic(int weight) {
		super(weight);
	}
	
	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;


	public static final BitVector[] GOOD_NEIGHBORHOODS = {
		new BitVector(NUMBER_OF_NEIGHBORHOODS),
		new BitVector(NUMBER_OF_NEIGHBORHOODS) };
	
//	public static final BitVector[] BAD_NEIGHBORHOODS = {
//		new BitVector(NUMBER_OF_NEIGHBORHOODS),
//		new BitVector(NUMBER_OF_NEIGHBORHOODS) };

	/**
	 * Set of 3x3 patterns taken from Gelly et al,
	 * "Modification of UCT with Patterns in Monte-Carlo Go"
	 */
	private static final Pattern[] PATTERN_LIST = {
		/**
		 * Good patterns
		 */
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
			new SimplePattern("#oO*??**"), // Edge2
		/**
		 * Bad patterns
		 */
			
//			// BLACK SPECIFIC PATTERNS
//			new ColorSpecificPattern("O.OO?oo?", BLACK), // Ponnuki 
//			new ColorSpecificPattern(".#..#.?.", BLACK), // Empty Triangle
//			new ColorSpecificPattern(".OO?OO??", BLACK), // Push through bamboo
//
//			// WHITE SPECIFIC PATTERNS
//			new ColorSpecificPattern("O.OO?oo?", WHITE), // Ponnuki 
//			new ColorSpecificPattern(".#..#.?.", WHITE), // Empty Triangle
//			new ColorSpecificPattern(".OO?OO??", WHITE) // Push through bamboo
			
	};

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
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			if (!isPossibleNeighborhood((char) i)) {
				continue;
			}
			for (int p = 0; p < 4; p++) {
				if (PATTERN_LIST[p].matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (int p = 4; p < 8; p++) {
				if (PATTERN_LIST[p].matches((char) i)) {
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			for (int p = 8; p < 15; p++) {
				if (PATTERN_LIST[p].matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			
//			for (int p = 15; p < 18; p++) {
//				if (PATTERN_LIST[p].matches((char) i)) {
//					BAD_NEIGHBORHOODS[BLACK].set(i, true);
//				}
//			}
//			for (int p = 18; p < PATTERN_LIST.length; p++) {
//				if (PATTERN_LIST[p].matches((char) i)) {
//					BAD_NEIGHBORHOODS[WHITE].set(i, true);
//				}
//			}
			
		}
	}

	public static final int evaluateMove(int color, char neighborhood) {
		if(GOOD_NEIGHBORHOODS[color].get(neighborhood)) {
			return 1;
		}
		// TODO Put these back in
//		if(BAD_NEIGHBORHOODS[color].get(neighborhood)) {
//			return -1;
//		}
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
		int[] values = getValues();
		for (int p : NEIGHBORS[board.getMove(board.getTurn() - 1)]) {
			if (board.getColor(p) == VACANT) {
				values[p] = evaluateMove(board.getColorToPlay(),board.getNeighborhood(p));
				if (values[p] != 0) {
					getNonzeroPoints().add(p);
					if ((getBestIndex() == -1) || (values[p] > values[getNonzeroPoints().get(getBestIndex())])) {
						setBestIndex(getNonzeroPoints().size() - 1);
					}
				}
			}
		}
	}

}
