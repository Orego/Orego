package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPatternHeuristic1 extends AbstractManhattanpatternheuristic {
	
	public ManhattanPatternHeuristic1(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[1]);
	}

}
