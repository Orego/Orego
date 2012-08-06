package orego.heuristic;

import static orego.core.Coordinates.*;

public class SquarePattern5Heuristic extends AbstractSquarePatternHeuristic {

	public SquarePattern5Heuristic(int weight) {
		super(weight);
		setRegion(SQUARE_NEIGHBORHOOD[5]);
	}

}
