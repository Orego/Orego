package orego.heuristic;

import ec.util.MersenneTwisterFast;
import orego.core.*;
import static orego.core.Colors.*;
import orego.util.*;

/** The value of a move is the number of stones it captures. */
public class CaptureHeuristic extends Heuristic {

	public CaptureHeuristic(int weight) {
		super(weight);
	}

	@Override
	public void prepare(Board board, MersenneTwisterFast random) {
		super.prepare(board, random);
		IntSet chains = board.getChainsInAtari(opposite(board.getColorToPlay()));
		for (int i = 0; i < chains.size(); i++) {
			int c = chains.get(i);
			increaseValue(board.getCapturePoint(c), board.getChainSize(c));
		}
	}

}
