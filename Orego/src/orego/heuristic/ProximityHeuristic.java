package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static java.lang.Math.*;

/** The value of move p is 1 if it is within 3 Manhattan distance of the previously played move, 0 otherwise. */
public class ProximityHeuristic extends Heuristic {

	public ProximityHeuristic(int weight) {
		super(weight);
	}

	@Override
	public int evaluate(int p, Board board) {
		if (board.getTurn() == 0) {
			return 0;
		}
		int lastMove = board.getMove(board.getTurn() - 1);
		if (abs(row(p) - row(lastMove)) + abs(column(p) - column(lastMove)) < 4) {
			return 1;
		}
		return 0;
	}

}
