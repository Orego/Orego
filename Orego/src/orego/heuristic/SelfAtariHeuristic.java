package orego.heuristic;

import orego.core.*;
import orego.util.*;

/**
 * Discourages self-atari.
 */
public class SelfAtariHeuristic extends Heuristic {

	public SelfAtariHeuristic(int weight) {
		super(weight);
	}

	@Override
	public void prepare(Board board) {
		super.prepare(board);
		IntSet vacantPoints = board.getVacantPoints();
		int color = board.getColorToPlay();
		for (int i = 0; i < vacantPoints.size(); i++) {
			int p = vacantPoints.get(i);
			if (board.isSelfAtari(p, color)) {
				discourage(p);
			}
		}
	}

}
