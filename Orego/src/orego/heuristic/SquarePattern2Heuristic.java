package orego.heuristic;

import static orego.core.Coordinates.*;

public class SquarePattern2Heuristic extends AbstractSquarePatternHeuristic {

	public SquarePattern2Heuristic(int weight) {
		super(weight);
		setRegion(SQUARE_NEIGHBORHOOD[2]);
	}

}
