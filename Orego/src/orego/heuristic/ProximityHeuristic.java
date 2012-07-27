package orego.heuristic;

import orego.core.*;


/** The value of move p is 1 if it is within a large knights move of the previously played move. */
public class ProximityHeuristic extends Heuristic {


	public ProximityHeuristic(int weight) {
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
