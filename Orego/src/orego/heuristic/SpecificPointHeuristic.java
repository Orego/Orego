package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;

/** Recommends c5. For testing only. */
public class SpecificPointHeuristic extends Heuristic {

	public SpecificPointHeuristic(int weight) {
		super(weight);
		recommend(at("c5"));
	}

	@Override
	public void prepare(Board board) {
		// Does nothing
	}
	
}
