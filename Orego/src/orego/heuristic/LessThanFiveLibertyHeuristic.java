package orego.heuristic;

import static orego.core.Colors.opposite;
import static orego.core.Coordinates.NEIGHBORS;
import orego.core.*;
import orego.util.IntList;


/** The value of move p is 1 if it is within a large knights move of the previously played move. */
public class LessThanFiveLibertyHeuristic extends Heuristic {

	private IntList targets;


	public LessThanFiveLibertyHeuristic(int weight) {
		super(weight);
		targets = new IntList(4);
	}

	@Override
	public int evaluate(int p, Board board) {
		if (board.getVacantNeighborCount(p) == 4) {
			return 0;
		}
		int result = 0;
		targets.clear();
		for (int i = 0; i < 4; i++) {
			int neighbor = NEIGHBORS[p][i];
			if(board.getColor(neighbor) == Colors.WHITE || board.getColor(neighbor) == Colors.BLACK){
				int target = board.getChainId(neighbor);
				int libertyCount = board.getLibertyCount(target);
				if (libertyCount < 5){
					targets.add(target);
					result += board.getChainSize(target) * (5 - libertyCount);
				}
			}
		}
		return result;
	}

}
