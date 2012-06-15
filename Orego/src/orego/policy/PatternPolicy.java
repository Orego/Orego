package orego.policy;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.*;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.core.Coordinates.ON_BOARD;
import static orego.patterns.Pattern.*;
import orego.core.Board;
import orego.mcts.SearchNode;
import orego.patterns.*;
import orego.util.*;
import ec.util.MersenneTwisterFast;

/**
 * Generates response moves to the previous play if a there are any pattern
 * matches in the immediate area.
 * 
 * @see orego.patterns
 */
public class PatternPolicy extends Policy {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;

	public static final BitVector[] GOOD_NEIGHBORHOODS = {
			new BitVector(NUMBER_OF_NEIGHBORHOODS),
			new BitVector(NUMBER_OF_NEIGHBORHOODS) };

	/**
	 * Set of 3x3 patterns taken from Gelly et al,
	 * "Modification of UCT with Patterns in Monte-Carlo Go"
	 */
	private static final Pattern[] PATTERN_LIST = {
			// BLACK SPECIFIC PATTERNS
			new ColorSpecificPattern("O...#O??", BLACK), // Hane4
			new ColorSpecificPattern("#??*?O**", BLACK), // Edge3
			new ColorSpecificPattern("O?+*?#**", BLACK), // Edge4
			new ColorSpecificPattern("O#O*?#**", BLACK), // Edge5
			// WHITE SPECIFIC PATTERNS
			new ColorSpecificPattern("O...#O??", BLACK), // Hane4
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
			new SimplePattern("#oO*??**") // Edge2
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
			for (int p = 8; p < PATTERN_LIST.length; p++) {
				if (PATTERN_LIST[p].matches((char) i)) {
					GOOD_NEIGHBORHOODS[BLACK].set(i, true);
					GOOD_NEIGHBORHOODS[WHITE].set(i, true);
				}
			}
		}
	}

	public static final boolean isGoodMove(int color, char neighborhood) {
		return GOOD_NEIGHBORHOODS[color].get(neighborhood);
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

	/** A set of moves determined to have high value from the pattern matcher. */
	private IntSet goodMoves;

	/** Defaults to random fallback if none is specified. */
	public PatternPolicy() {
		this(new RandomPolicy());
	}

	public PatternPolicy(Policy fallback) {
		super(fallback);
		goodMoves = new IntSet(FIRST_POINT_BEYOND_BOARD);
	}

	public Policy clone() {
		PatternPolicy result = (PatternPolicy) super.clone();
		result.goodMoves = new IntSet(FIRST_POINT_BEYOND_BOARD);
		return result;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int lastPlay = board.getMove(board.getTurn() - 1);
		if (ON_BOARD[lastPlay]) {
			// clear old moves
			goodMoves.clear();
			// populate new good moves from the eight neighbors of the previous
			// play
			int[] n = NEIGHBORS[lastPlay];
			for (int i = 0; i < n.length; i++) {
				if (ON_BOARD[n[i]]
						&& board.getColor(n[i]) == VACANT
						&& isGoodMove(board.getColorToPlay(),
								board.getNeighborhood(n[i]))) {
					goodMoves.addKnownAbsent(n[i]);
				}
			}
			// starting from a random point in good moves, return the first
			// available good move
			if (goodMoves.size() == 0) {
				return getFallback().selectAndPlayOneMove(random, board);
			}
			int start = random.nextInt(goodMoves.size());
			for (int i = start; i < goodMoves.size(); i++) {
				int p = goodMoves.get(i);
				if (board.isFeasible(p) && board.playFast(p) == PLAY_OK) {
					return p;
				}
			}
			for (int i = 0; i < start; i++) {
				int p = goodMoves.get(i);
				if (board.isFeasible(p) && board.playFast(p) == PLAY_OK) {
					return p;
				}
			}
			// The two for loops above could be replaced with this. See the same
			// method in
			// RandomPolicy for an explanation.
			// int i = start;
			// do {
			// int p = goodMoves.get(i);
			// if (board.isFeasible(p) && board.playFast(p) == PLAY_OK) {
			// return p;
			// }
			// i = (i + 457) % goodMoves.size();
			// } while (i != start);
		} // end if
		return getFallback().selectAndPlayOneMove(random, board);
	}

	public void updatePriors(SearchNode node, Board board, int weight) {
		// Update priors for the eight neighbors of the previous play
		int lastPlay = board.getMove(board.getTurn() - 1);
		int[] n = NEIGHBORS[lastPlay];
		for (int i = 0; i < n.length; i++) {
			if (ON_BOARD[n[i]]
					&& board.getColor(n[i]) == VACANT
					&& isGoodMove(board.getColorToPlay(),
							board.getNeighborhood(n[i]))) {
				node.addWins(n[i], weight);
			}
		}
		getFallback().updatePriors(node, board, weight);
	}

}
