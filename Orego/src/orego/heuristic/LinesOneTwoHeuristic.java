package orego.heuristic;

import orego.core.Board;
import static orego.core.Coordinates.*;

public class LinesOneTwoHeuristic extends Heuristic{

	public LinesOneTwoHeuristic(int weight) {
		super(weight);
	}
	
	@Override
	public int evaluate(int p, Board board) {
		// if the point is in one of the first two or last two columns on the
		// board, and it is in one of the first two or last two rows, then it is
		// on the first or second line, so return -1.
		return  column(p) == 0   ||
				column(p) == 1   ||
				column(p) == 17  ||
				column(p) == 18  ||
				row(p) == 0      ||
				row(p) == 1      ||
				row(p) == 17     ||
				row(p) == 18 ? -1 : 0;
	}

}
