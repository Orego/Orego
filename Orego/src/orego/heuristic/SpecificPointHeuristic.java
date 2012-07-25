package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

/** The value of a move is the number of stones it captures. */
public class SpecificPointHeuristic implements Heuristic {

	/** List of chains that would be saved by this move. */
	private int point;

	public SpecificPointHeuristic() {
		point = at("a1");
	}

	public SpecificPointHeuristic(int p) {
		point = p;
	}

	@Override
	public int evaluate(int p, Board board) {
		if (p == point) {
			return 1;
		}
		return 0;
	}

}
