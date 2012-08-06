package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPatternHeuristic4 extends AbstractManhattanpatternheuristic {
	
	public ManhattanPatternHeuristic4(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[4]);
	}

}
