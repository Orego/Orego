package orego.policy;

import static orego.core.Board.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static orego.patterns.Pattern.diagramToNeighborhood;
import orego.core.Board;
import orego.mcts.SearchNode;
import ec.util.MersenneTwisterFast;

public class SidePolicy extends Policy {

	public SidePolicy() {
		this(new RandomPolicy());
	}

	public SidePolicy(Policy fallback) {
		super(fallback);
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		int enemy = opposite(board.getColorToPlay());
		int[] top = new int[13];
		int[] bottom = new int[13];
		int[] leftSide = new int[13];
		int[] rightSide = new int[13];
		for (int i = 4; i < 18; i++) {
			top[i - 4] = at(17, i);
			bottom[i - 4] = at(3, i);
			leftSide[i - 4] = at(i, 17);
			rightSide[i - 4] = at(i, 3);
		}
		for (int p = 0; p < rightSide.length; p++) {
			if(!enemiesNearby(top[p], board, enemy)){
				node.addWins(p, 10 * weight);
			}			
			if(!enemiesNearby(bottom[p], board, enemy)){
				node.addWins(p, 10 * weight);
			}	
			if(!enemiesNearby(leftSide[p], board, enemy)){
				node.addWins(p, 10 * weight);
			}	
			if(!enemiesNearby(rightSide[p], board, enemy)){
				node.addWins(p, 10 * weight);
			}	
		}
		getFallback().updatePriors(node, board, weight);
	}

	private boolean enemiesNearby(int p, Board board, int enemy) {
		for (int r = -2; r <= 1; r++) {
			for (int c = -2; c <= 2; c++) {
				if (board.getColor(at(p + r,p + c)) == enemy) {
					return true;
				}
			}
		}
		return false;
	}
}
