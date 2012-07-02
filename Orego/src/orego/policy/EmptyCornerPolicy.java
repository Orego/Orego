package orego.policy;

import static orego.core.Colors.opposite;
import static orego.core.Coordinates.at;
import static orego.core.Coordinates.*;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;

import java.util.Iterator;

import orego.core.Board;
import orego.mcts.SearchNode;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

public class EmptyCornerPolicy extends Policy {

	public EmptyCornerPolicy() {
		this(new RandomPolicy());
	}

	public EmptyCornerPolicy(Policy fallback) {
		super(fallback);

	}

	public Policy clone() {
		EmptyCornerPolicy result = (EmptyCornerPolicy) super.clone();
		return result;
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		int enemy = opposite(board.getColorToPlay());
		int corners[] = { at("e5"), at("e15"), at("p5"), at("p15") };
		int matches[] = { at("d4"), at("d16"), at("q4"), at("q16") };
		for (int i = 0; i<corners.length; i++) {
			if (hasNoNearbyEnemies(corners[i], enemy, board)) {
				node.addWins(matches[i], 10*weight);
			}
		}
	}

	private boolean hasNoNearbyEnemies(int corner, int enemyColor, Board board) {
		for(int i = -4; i<=4; i++){
			for (int j = -4; j <=4 ; j++) {
				int r = row(corner);
				int c = column(corner);
				if(board.getColor((at(r + i, c + j)))==enemyColor){
					return false;
				}
			}
		}
		return true;
	}

}
