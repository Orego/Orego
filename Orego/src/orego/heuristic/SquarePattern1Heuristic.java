package orego.heuristic;

import static orego.core.Coordinates.*;

public class SquarePattern1Heuristic extends AbstractSquarePatternHeuristic {

	public SquarePattern1Heuristic(int weight) {
		super(weight);
		setRegion(SQUARE_NEIGHBORHOOD[1]);
	}

}
