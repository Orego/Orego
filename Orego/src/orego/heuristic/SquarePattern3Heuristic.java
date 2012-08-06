package orego.heuristic;

import static orego.core.Coordinates.*;

public class SquarePattern3Heuristic extends AbstractSquarePatternHeuristic {

	public SquarePattern3Heuristic(int weight) {
		super(weight);
		setRegion(SQUARE_NEIGHBORHOOD[3]);
	}

}
