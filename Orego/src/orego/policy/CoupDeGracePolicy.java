package orego.policy;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.core.Board;
import orego.mcts.SearchNode;
import ec.util.MersenneTwisterFast;

/**
 * Suggests moving on any point next to an enemy stone with < 5 liberties. The
 * intent is to encourage capturing dead stone after the game is effectively
 * over, as a courtesy to human opponents.
 * (This policy only updates priors; it does not select moves.)
 */
public class CoupDeGracePolicy extends Policy {

	public CoupDeGracePolicy() {
		super(new RandomPolicy());
	}

	public CoupDeGracePolicy(Policy fallback) {
		super(fallback);
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		int enemy = opposite(board.getColorToPlay());
		for (int p : ALL_POINTS_ON_BOARD) {
			if (board.getColor(p) == enemy) {
				for (int i = 0; i < 4; i++) {
					int n = NEIGHBORS[p][i];
					if (ON_BOARD[n]) {
						node.addWins(n, weight * 12 / board.getLibertyCount(p));
					}
				}
			}
		}
		getFallback().updatePriors(node, board, weight);
	}

}
