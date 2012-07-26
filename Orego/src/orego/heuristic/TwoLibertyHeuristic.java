package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

/**
 * The value of a move is either the number of stones secured or the number of
 * stones that can be captured.
 */
public class TwoLibertyHeuristic extends Heuristic {

	/**
	 * String we have looked at.
	 */
	private IntList targets;

	public TwoLibertyHeuristic(double weight) {
		super(weight);
		targets = new IntList(4);
	}

	@Override
	public int evaluate(int p, Board board) {
		targets.clear();
		int result = 0;
		for (int i = 0; i < 4; i++) {
			int neighbor = NEIGHBORS[p][i];
			if (board.getColor(neighbor) == BLACK || board.getColor(neighbor) == WHITE) {
				int target = board.getChainId(neighbor);
				if (board.getLibertyCount(target) == 2) {
					IntSet liberties = board.getLiberties(target);
					int loc = (liberties.get(0) == p) ? 0 : 1;
					int lib1 = liberties.get(loc);
					int lib2 = liberties.get(1-loc);
					int nblib1 = board.getVacantNeighborCount(lib1) + 1;
					int nblib2 = board.getVacantNeighborCount(lib2) + 1;
					for (int k = 0; k < 4; k++) {	
						int neighbor1 = NEIGHBORS[lib1][k];
						int neighbor2 = NEIGHBORS[lib2][k];
						if(board.getColor(neighbor1)==board.getColor(target) && board.getChainId(neighbor1)!=target) {
							nblib1 += board.getLibertyCount(neighbor1);
						}
						if(board.getColor(neighbor2)==board.getColor(target) && board.getChainId(neighbor2)!=target) {
							nblib2 += board.getLibertyCount(neighbor2);
						}
						if (neighbor1 == lib2) {
							nblib1--;
							nblib2--;
							break;
						}
					}
					
					if (nblib2 <= 2 && nblib1 > nblib2) {
						result += board.getChainSize(target);
					}
					if ((nblib1 <= 2 && nblib1 == nblib2) && board.getColorToPlay() != board.getColor(target)) {
						result += board.getChainSize(target);
					}
					targets.add(target);
				}
			}
		}
		return result;
	}
	
}
