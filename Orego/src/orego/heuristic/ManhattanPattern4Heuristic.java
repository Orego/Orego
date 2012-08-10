package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPattern4Heuristic extends AbstractManhattanPatternHeuristic {
	
	public ManhattanPattern4Heuristic(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[4]);
	}

}
