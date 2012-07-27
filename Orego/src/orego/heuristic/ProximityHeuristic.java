package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import static orego.core.Board.*;
import orego.util.*;

/** The value of move p is 1 if it is within a large knights move of the previously played move. */
public class ProximityHeuristic extends Heuristic {


	public ProximityHeuristic(double weight) {
		super(weight);
	}

	@Override
	public int evaluate(int p, Board board) {
		for (int inLargeKnights : Coordinates.LARGE_KNIGHT_NEIGHBORHOOD[board
				.getMove(board.getTurn() - 1)]) {
			if (p == inLargeKnights) {
				return 1;
			}
		}
		return 0;
	}

}
