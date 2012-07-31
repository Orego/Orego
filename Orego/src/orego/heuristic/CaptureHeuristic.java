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

	public void prepare(Board board) {
		super.prepare(board);
		IntSet vacant = board.getVacantPoints();
		int[] values = getValues();
		for (int i = 0; i < vacant.size(); i++) {
			int p = vacant.get(i);
			values[p] = beforeEvaluate(p, board);
			if (values[p] != 0) {
				getNonzeroPoints().add(p);
				if ((getBestIndex() == -1) || (values[p] > values[getBestIndex()])) {
					setBestIndex(getNonzeroPoints().size() - 1);
				}
			}
		}
	}

	// TODO Make this more efficient by looking at chains instead of points next to them
	public int beforeEvaluate(int p, Board board) {
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
