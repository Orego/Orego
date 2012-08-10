package orego.heuristic;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;
import orego.core.*;

public class PatternHeuristic extends AbstractPatternHeuristic {
	
	public PatternHeuristic(int weight) {
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
	
	@Override
	public boolean isBad(int p, Board board) {
		return BAD_NEIGHBORHOODS[board.getColorToPlay()].get(board.getNeighborhood(p));
	}

}
