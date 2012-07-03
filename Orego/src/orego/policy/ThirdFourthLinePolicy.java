package orego.policy;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.*;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.*;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;
import static orego.patterns.Pattern.diagramToNeighborhood;

import java.util.Iterator;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

/**
 * The Third Fourth Line policy, selecting moves on the third or fourth line
 * which have a fully vacant 3x3 neighborhood.
 */
public class ThirdFourthLinePolicy extends Policy {

	public static final char VACANT_NEIGHBORHOOD = diagramToNeighborhood("...\n. .\n...");

	public ThirdFourthLinePolicy() {
		this(new RandomPolicy());
	}

	public ThirdFourthLinePolicy(Policy fallback) {
		super(fallback);

	}

	public Policy clone() {
		ThirdFourthLinePolicy result = (ThirdFourthLinePolicy) super.clone();
		return result;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		if (board.getTurn() < 50) {
			int start = random.nextInt(THIRD_AND_FOURTH_LINE_POINTS.length);
			int i = start;
			do {
				int p = THIRD_AND_FOURTH_LINE_POINTS[i];
				if ((board.getColor(p) == VACANT)
						&& (board.getNeighborhood(p) == VACANT_NEIGHBORHOOD)
						&& (board.playFast(p) == PLAY_OK)) {
					return p;
				}
				i = (i + 457) % THIRD_AND_FOURTH_LINE_POINTS.length;
			} while (i != start);
		}
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		if (board.getTurn() < 50) {
			for (int i = 0; i < THIRD_AND_FOURTH_LINE_POINTS.length; i++) {
				int p = THIRD_AND_FOURTH_LINE_POINTS[i];
				if ((board.getColor(p) == VACANT)
						&& (board.getNeighborhood(p) == VACANT_NEIGHBORHOOD)) {
					node.addWins(p, weight);
				}
			}
		}
		getFallback().updatePriors(node, board, weight);
	}

}
