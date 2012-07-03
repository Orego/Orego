package orego.policy;

import static orego.core.Colors.opposite;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.*;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;

import java.util.Iterator;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

/**
 * The Empty Corner Policy, updating the priors on the 4 4 points if there are
 * no nearby enemy stones in the joseki corner. Not including the center
 * boundary lines.
 */
public class EmptyCornerPolicy extends Policy {

	public static int CORNERS[] = { at("e5"), at("e15"), at("p5"), at("p15") };
	public static int MATCHES[] = { at("d4"), at("d16"), at("q4"), at("q16") };

	public EmptyCornerPolicy() {
		this(new RandomPolicy());
	}

	public EmptyCornerPolicy(Policy fallback) {
		super(fallback);

	}

	public Policy clone() {
		EmptyCornerPolicy result = (EmptyCornerPolicy) super.clone();
		return result;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		if (board.getTurn() < 50) {
			int enemy = opposite(board.getColorToPlay());

			for (int i = 0; i < CORNERS.length; i++) {
				if (hasNoNearbyEnemies(CORNERS[i], enemy, board)) {
					node.addWins(MATCHES[i], 10 * weight);
				}
			}
		}
		getFallback().updatePriors(node, board, weight);
	}

	/** Returns true if corner has no nearby enemy stones */
	private boolean hasNoNearbyEnemies(int corner, int enemyColor, Board board) {
		for (int i = -4; i <= 4; i++) {
			for (int j = -4; j <= 4; j++) {
				int r = row(corner);
				int c = column(corner);
				if (board.getColor((at(r + i, c + j))) == enemyColor) {
					return false;
				}
			}
		}
		return true;
	}

}
