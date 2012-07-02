package orego.policy;

import static orego.core.Board.*;
import static orego.core.Coordinates.*;
import orego.core.Board;
import orego.mcts.SearchNode;
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
//		for (int i = start; i < vacantPoints.size(); i++) {
//			int p = vacantPoints.get(i);
//			if (board.isFeasible(p) && board.playFast(p) == PLAY_OK) {
//				return p;
//			}
//		}
//		for (int i = 0; i < start; i++) {
//			int p = vacantPoints.get(i);
//			if (board.isFeasible(p) && board.playFast(p) == PLAY_OK) {
//				return p;
//			}
//		}
		// TODO Run experiments to see if the suggestion below matters. It
		// doesn't appear to affect speed, and it's simpler, so if it doesn't
		// hurt, we should use it.
		//
		// One way to fix the uneven distribution (C17 is massively overselected
		// at the beginning of the game) is to replace the two for loops above
		// with this code, based on the idea of double hashing. (457, being
		// prime and larger than the largest board area, must be relatively
		// prime with the size of vacantPoints.) Another might be to keep a list
		// of feasible points. Preliminary tests indicate that it doesn't really
		// affect playing strength.
		int i = start;
		do {
			int p = vacantPoints.get(i);
			if (board.isFeasible(p) && board.playFast(p) == PLAY_OK) {
				return p;
			}
			i = (i + 457) % vacantPoints.size();
		} while (i != start);
		board.play(PASS);
		return PASS;
	}

	public void updatePriors(SearchNode node, Board board, int weight) {
		// Does nothing
	}

}
