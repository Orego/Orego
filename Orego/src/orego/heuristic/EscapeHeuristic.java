package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

/** The value of a move is the number of stones saved * the number of liberties after saving - 1. */
public class EscapeHeuristic extends Heuristic {

	/** List of chains that would be saved by this move. */
	private IntList targets;

	public EscapeHeuristic(double weight) {
		setWeight(weight);
		targets = new IntList(4);
	}

	@Override
	public int evaluate(int p, Board board) {
		int color = board.getColorToPlay();
		if (board.getNeighborCount(p, color) == 0) {
			return 0;
		}
		int result = 0;
		int multiplier = 0;
		targets.clear();
		for (int i = 0; i < 4; i++) {
			int neighbor = NEIGHBORS[p][i];
			if (board.getColor(neighbor) == color) { //if the neighbor is our color
				int target = board.getChainId(neighbor);
				if ((board.isInAtari(target))
						&& (!targets.contains(target))) {
					targets.add(target);
					result += board.getChainSize(target);
				}
				else{
					multiplier += board.getLibertyCount(neighbor) - 1;
				}
			}
		}
		multiplier += board.getVacantNeighborCount(p);
		result *= (multiplier - 1);
		return result;
	}

}
