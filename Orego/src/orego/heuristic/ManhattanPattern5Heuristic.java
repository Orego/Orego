package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPattern5Heuristic extends AbstractManhattanPatternHeuristic {
	
	public ManhattanPattern5Heuristic(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[5]);
	}

}
