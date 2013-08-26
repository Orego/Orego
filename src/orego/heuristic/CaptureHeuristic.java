package orego.heuristic;

import orego.core.*;
import static orego.core.Colors.*;
import orego.util.*;

/** The value of a move is the number of stones it captures. */
public class CaptureHeuristic extends Heuristic {

	public CaptureHeuristic(int weight) {
		super(weight);
	}

	@Override
	public void prepare(Board board) {
		super.prepare(board);
		IntSet chains = board.getChainsInAtari(opposite(board.getColorToPlay()));
		for (int i = 0; i < chains.size(); i++) {
			recommend(board.getCapturePoint(chains.get(i)));
		}
	}

	@Override
	public CaptureHeuristic clone() {
		return (CaptureHeuristic)super.clone();
	}

}
