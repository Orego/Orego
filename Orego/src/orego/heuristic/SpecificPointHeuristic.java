package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

/** Returns the number of stones on the board for c5, 0 for all other points. For testing only. */
public class SpecificPointHeuristic extends Heuristic {

	public SpecificPointHeuristic(int weight) {
		super(weight);
	}

	@Override
	public void prepare(Board board) {
		super.prepare(board);
		getValues()[at("c5")] = board.getStoneCounts()[BLACK] + board.getStoneCounts()[WHITE];
		getNonzeroPoints().add(at("c5"));
		setBestMove(at("c5"));
	}
	
}
