package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPatternHeuristic3 extends AbstractManhattanpatternheuristic {
	
	public ManhattanPatternHeuristic3(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[3]);
	}

}
