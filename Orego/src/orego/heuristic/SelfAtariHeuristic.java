package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import orego.util.*;

/**
 * The value of a move is as negative as the number of stones it puts in
 * self-atari including the point itself.
 */
public class SelfAtariHeuristic extends Heuristic {

	/** List of chains already seen in atari. */
	private IntList targets;

	public SelfAtariHeuristic(double weight) {
		super(weight);
		targets = new IntList(4);
	}

	@Override
	public int evaluate(int p, Board board) {
		int result = 0;
		targets.clear();
		int color = board.getColorToPlay();
		if (board.isSelfAtari(p, color)) {
			for (int i = 0; i < 4; i++) {
				int neighbor = NEIGHBORS[p][i];
				if (board.getColor(neighbor) == color) { // if the neighbor is our color
					int target = board.getChainId(neighbor);
					if (!targets.contains(target)) {
						result -= board.getChainSize(target);
						targets.add(target);
					}
				}
			}
		} else {
			return 0;
		}
		return result - 1;
	}
}
