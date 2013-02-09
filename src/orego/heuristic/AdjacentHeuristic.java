package orego.heuristic;

import orego.core.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.assertTrue;

public class AdjacentHeuristic extends Heuristic {

	public AdjacentHeuristic(int weight) {
		super(weight);
	}
	
	@Override
	public void prepare(Board board) {
		super.prepare(board);
		int previousSCMove = board.getMove(board.getTurn() - 2);
		for(int i = 0; i < 4; i++) {
			if(board.getColor(NEIGHBORS[previousSCMove][i]) == VACANT) {
				recommend(NEIGHBORS[previousSCMove][i]);
			}
		}	
	}
	
	@Override
	public AdjacentHeuristic clone() {
		return (AdjacentHeuristic)super.clone();
	}
	
}
