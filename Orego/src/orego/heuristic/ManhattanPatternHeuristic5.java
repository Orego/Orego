package orego.heuristic;


import static orego.core.Coordinates.*;

public class ManhattanPatternHeuristic5 extends AbstractManhattanpatternheuristic {
	
	public ManhattanPatternHeuristic5(int weight){
		super(weight);
		setRegion(MANHATTAN_NEIGHBORHOOD[5]);
	}

}
