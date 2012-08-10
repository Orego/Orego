package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPattern1Heuristic extends AbstractManhattanPatternHeuristic {
	
	public ManhattanPattern1Heuristic(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[1]);
	}

}
