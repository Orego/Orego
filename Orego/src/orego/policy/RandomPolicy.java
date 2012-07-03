package orego.policy;

import static orego.core.Board.*;
import static orego.core.Coordinates.*;
import orego.core.Board;
import orego.mcts.SearchNode;
import orego.response.ResponsePlayer;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

/**
 * Generates random moves (but avoids playing in its own eyes). This policy
 * always generates a move, so any later policies will only affect priors.
 */
public class RandomPolicy extends Policy {

	public RandomPolicy() {
		super(null);
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		// TODO Find other place where similar code appears and make the fix below
		int i = start;
		do {
			int p = vacantPoints.get(i);
			if (board.isFeasible(p) && board.playFast(p) == PLAY_OK) {
				return p;
			}
			// The magic number 457 is prime and larger than vacantPoints.size().
			// Advancing by 457 therefore skips "randomly" through the array,
			// in a manner analogous to double hashing.
			i = (i + 457) % vacantPoints.size();
		} while (i != start);
		board.play(PASS);
		return PASS;
	}

	public void updatePriors(SearchNode node, Board board, int weight) {
		// Does nothing
	}

	public void updateResponses(ResponsePlayer player, Board board, int weight) {
		// Does nothing
	}
}
