package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.ON_BOARD;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.core.Board;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Cut1Pattern;
import orego.patterns.Pattern;
import orego.patterns.SimplePattern;
import orego.util.BitVector;

public class BadPatternHeuristic extends Heuristic {
	
	public BadPatternHeuristic(double weight) {
		super(weight);
	}
	
	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;

	public static final BitVector[] BAD_NEIGHBORHOODS = {
			new BitVector(NUMBER_OF_NEIGHBORHOODS),
			new BitVector(NUMBER_OF_NEIGHBORHOODS) };

	/**
	 * Set of 3x3 patterns taken from Gelly et al,
	 * "Modification of UCT with Patterns in Monte-Carlo Go"
	 */
	private static final Pattern[] PATTERN_LIST = {
			// BLACK SPECIFIC PATTERNS
			new ColorSpecificPattern("O.OO?oo?", BLACK), // Ponnuki 
			new ColorSpecificPattern(".#..#.?.", BLACK), // Empty Triangle
			new ColorSpecificPattern(".OO?OO??", BLACK), // Push through bamboo

			// WHITE SPECIFIC PATTERNS
			new ColorSpecificPattern("#.##?++?", WHITE), // Ponnuki 
			new ColorSpecificPattern(".O..O.?.", WHITE), // Empty Triangle
			new ColorSpecificPattern(".##?##??", WHITE), // Push through bamboo

			// Color independent patterns
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
		// Find all bad neighborhoods, i.e., neighborhoods where a player
		// should not play.
		// Note that i has to be an int, rather than a char, because
		// otherwise incrementing it after Character.MAX_VALUE would
		// return it to 0, resulting in an infinite loop.
		for (int i = 0; i < NUMBER_OF_NEIGHBORHOODS; i++) {
			if (!isPossibleNeighborhood((char) i)) {
				continue;
			}
			for (int p = 0; p < 4; p++) {
				if (PATTERN_LIST[p].matches((char) i)) {
					BAD_NEIGHBORHOODS[BLACK].set(i, true);
				}
			}
			for (int p = 4; p < 8; p++) {
				if (PATTERN_LIST[p].matches((char) i)) {
					BAD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
			for (int p = 8; p < PATTERN_LIST.length; p++) {
				if (PATTERN_LIST[p].matches((char) i)) {
					BAD_NEIGHBORHOODS[BLACK].set(i, true);
					BAD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
		}
	}

	public static final boolean isBadMove(int color, char neighborhood) {
		return BAD_NEIGHBORHOODS[color].get(neighborhood);
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
	public int evaluate(int p, Board board) {
		if (board.getColor(p) == VACANT
				&& isBadMove(board.getColorToPlay(),
						board.getNeighborhood(p))) {
			return -1;
		}
		return 0;
	}

}
