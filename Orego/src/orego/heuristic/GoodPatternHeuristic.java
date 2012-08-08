package orego.heuristic;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;
import orego.core.*;

public class GoodPatternHeuristic extends AbstractPatternHeuristic {
	
	public GoodPatternHeuristic(int weight) {
		super(weight);
	}
	
	@Override
	public void prepare(Board board) {
		super.prepare(board);
		int lastMove = board.getMove(board.getTurn() - 1);
		if (!ON_BOARD[lastMove]) {
			return;
		}
		for (int p : NEIGHBORS[lastMove]) {
			if (board.getColor(p) == VACANT) {
				char neighborhood = board.getNeighborhood(p);
				if(GOOD_NEIGHBORHOODS[board.getColorToPlay()].get(neighborhood)) {
					recommend(p);
				}
			}
		}
	}

}
