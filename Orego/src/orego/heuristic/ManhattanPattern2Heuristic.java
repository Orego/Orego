package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPattern2Heuristic extends AbstractManhattanPatternHeuristic {
	
	public ManhattanPattern2Heuristic(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[2]);
	}

}
