package orego.heuristic;

import static orego.core.Coordinates.getFirstPointBeyondBoard;
import orego.core.Board;
import orego.util.IntSet;

public class DeepPatternHeuristic extends Heuristic{
	
	
	public DeepPatternHeuristic(int weight) {
		super(weight);
	}
	
	public void prepare(Board board){
		super.prepare(board);
		
		IntSet moves = board.getVacantPoints();
		for (int i = 0; i < moves.size(); i++) {
			if (board.isLegal(moves.get(i)) && board.isFeasible(moves.get(i))) {
				float tempRate = (float) getWinRate(board, moves.get(i));
				if (tempRate > rate) {
					rate = tempRate;
					result = moves.get(i);
				}
			}
		}
	}

}
