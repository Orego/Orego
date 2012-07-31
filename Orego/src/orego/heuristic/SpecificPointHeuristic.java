package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

/** Returns the number of stones on the board for c5, 0 for all other points. For testing only. */
public class SpecificPointHeuristic extends Heuristic {

	/** Number of stones on the board. */
	private int count;
	
	public SpecificPointHeuristic(int weight) {
		super(weight);
	}
	
	@Override
	public int evaluate(int p, Board board) {
		if (p == at("c5")) {
			return count;
		}
		return 0;
	}

	@Override
	public void prepare(Board board) {
		count = board.getStoneCounts()[BLACK] + board.getStoneCounts()[WHITE];
	}
	
}
