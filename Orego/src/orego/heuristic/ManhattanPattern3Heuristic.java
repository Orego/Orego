package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPattern3Heuristic extends AbstractManhattanPatternHeuristic {
	
	public ManhattanPattern3Heuristic(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[3]);
	}

}
