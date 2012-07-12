package orego.policy;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;
import static orego.core.Board.PLAY_OK;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

/**
 * This policy suggests placing opponents stones in atari if there is currently
 * a ko fight.
 */
public class KoAtariPolicy extends Policy {

	public KoAtariPolicy() {
		this(new RandomPolicy());
	}

	public KoAtariPolicy(Policy fallback) {
		super(fallback);
	}

	/**
	 * Returns a list of the possible moves that will place a group of the
	 * opponent's stones in atari
	 */
	protected IntSet atari(Board board) {
		IntSet moves = new IntSet(FIRST_POINT_BEYOND_BOARD);
		for (int point : ALL_POINTS_ON_BOARD) {
			if (board.getColor(point) == opposite(board.getColorToPlay())
					&& board.getLibertyCount(point) == 2) {
				moves.add(board.getLiberties(point).get(0));
				moves.add(board.getLiberties(point).get(1));
			}
		}
		return moves;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		if (board.getKoPoint() != NO_POINT) {
			IntSet moves = atari(board);
			int start = random.nextInt(moves.size());
			int i = start;
			do {
				int p = moves.get(i);
				// An atari must be feasible
				if (board.playFast(p) == PLAY_OK) {
					return p;
				}
				// The magic number 457 is prime and larger than
				// moves.size().
				// Advancing by 457 therefore skips "randomly" through the
				// array,
				// in a manner analogous to double hashing.
				i = (i + 457) % moves.size();
			} while (i != start);
		}
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		if (board.getKoPoint() != NO_POINT) {
			IntSet moves = atari(board);
			for (int i = 0; i < moves.size(); i++) {
				node.addWins(moves.get(i), weight * 7);
			}
		}
		getFallback().updatePriors(node, board, weight);
	}

}
