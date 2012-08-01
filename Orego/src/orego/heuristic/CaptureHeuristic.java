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
		IntSet chains = board.getChainsInAtari(opposite(board.getColorToPlay()));
		int[] values = getValues();
		for (int i = 0; i < chains.size(); i++) {
			int c = chains.get(i);
			int p = board.getCapturePoint(c);
			if (getNonzeroPoints().contains(p)) {
				values[p] += board.getChainSize(c);
			} else {
				values[p] = board.getChainSize(c);
				getNonzeroPoints().add(p);
			}
			if ((getBestIndex() == -1) || (values[p] > values[getNonzeroPoints().get(getBestIndex())])) {
				setBestIndex(getNonzeroPoints().size() - 1);
			}		
		}
	}

}
