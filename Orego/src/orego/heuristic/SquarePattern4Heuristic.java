package orego.heuristic;

import static orego.core.Coordinates.*;

public class SquarePattern4Heuristic extends AbstractSquarePatternHeuristic {

	public SquarePattern4Heuristic(int weight) {
		super(weight);
		setRegion(SQUARE_NEIGHBORHOOD[4]);
	}

}
