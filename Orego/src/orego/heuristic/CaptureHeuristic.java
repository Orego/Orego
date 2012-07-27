package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

/** The value of a move is the number of stones it captures. */
public class CaptureHeuristic extends Heuristic {

	/** List of chains that would be captured by this move. */
	private IntList targets;

	public CaptureHeuristic(int weight) {
		super(weight);
		targets = new IntList(4);
	}

	@Override
	public int evaluate(int p, Board board) {
		int enemy = opposite(board.getColorToPlay());
		if (board.getNeighborCount(p, enemy) == 0) {
			return 0;
		}
		int result = 0;
		targets.clear();
		for (int i = 0; i < 4; i++) {
			int neighbor = NEIGHBORS[p][i];
			if (board.getColor(neighbor) == enemy) {
				int target = board.getChainId(neighbor);
				if ((board.isInAtari(target))
						&& (!targets.contains(target))) {
					targets.add(target);
					result += board.getChainSize(target);
				}
			}
		}
		return result;
	}

}
