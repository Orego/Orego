package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

/** Returns 1 for b2, 0 for all other points. For testing only. */
public class SpecificPointHeuristic extends Heuristic {

	public SpecificPointHeuristic(int weight) {
		super(weight);
	}
	
	@Override
	public int evaluate(int p, Board board) {
		if (p == at("c5")) {
			return 1;
		}
		return 0;
	}

}
