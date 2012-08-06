package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPatternHeuristic2 extends AbstractManhattanpatternheuristic {
	
	public ManhattanPatternHeuristic2(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[2]);
	}

}
