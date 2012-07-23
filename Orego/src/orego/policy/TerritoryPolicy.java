package orego.policy;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.Board;
import orego.core.Coordinates;
import orego.mcts.SearchNode;
import ec.util.MersenneTwisterFast;

public class TerritoryPolicy extends Policy {

	int[] weights;
	
	final static int TERRITORY_WEIGHT = 7;

	public TerritoryPolicy() {
		this(new RandomPolicy());
	}

	public TerritoryPolicy(Policy fallback) {
		super(fallback);
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		return getFallback().selectAndPlayOneMove(random, board);
	}

	@Override
	public void updatePriors(SearchNode node, Board board, int weight) {
		weights = new int[EXTENDED_BOARD_AREA];
		for (int p : ALL_POINTS_ON_BOARD) {
			if (board.getColor(p) == BLACK) {
				weights[p] = 64;
			}
			if (board.getColor(p) == WHITE) {
				weights[p] = -64;
			}
		}
		dilation(weights);
		erosion(weights);
		for (int p : ALL_POINTS_ON_BOARD) {
			if (weights[p] != 0){
				node.addLosses(p, weight * TERRITORY_WEIGHT);
			}
		}
	}
	
	public int[] calculateTerritory(Board board) {
		weights = new int[EXTENDED_BOARD_AREA];
		for (int p : ALL_POINTS_ON_BOARD) {
			if (board.getColor(p) == BLACK) {
				weights[p] = 64;
			}
			if (board.getColor(p) == WHITE) {
				weights[p] = -64;
			}
		}
		dilation(weights);
		erosion(weights);
		return weights;
	}

	public void dilation(int[] weights) {
		int[] newweights = new int[weights.length];
		for (int i = 0; i < newweights.length; i++) {
			newweights[i] = weights[i];
		}
		for (int repeat = 0; repeat < 4; repeat++) {
			for (int p : ALL_POINTS_ON_BOARD) {
				for (int i = 0; i < 4; i++) {
					int n = NEIGHBORS[p][i];
					if (Coordinates.ON_BOARD[n]) {
						if (weights[n] > 0) {
							newweights[p]++;
						}
						if (weights[n] < 0) {
							newweights[p]--;
						}
					}
					else {
						if (weights[p] > 0) {
							newweights[p] ++;
						}
						if (weights[p] < 0) {
							newweights[p] --;
						}
					}
				}
			}
			for (int i = 0; i < newweights.length; i++) {
				weights[i] = newweights[i];
			}
		}
	}
	
	public void erosion(int[] weights) {
		int[] newweights = new int[weights.length];
		for (int i = 0; i < newweights.length; i++) {
			newweights[i] = weights[i];
		}
		for (int repeat = 0; repeat < 8; repeat++) {
			for (int p : ALL_POINTS_ON_BOARD) {
				for (int i = 0; i < 4; i++) {
					int n = NEIGHBORS[p][i];
					if (Coordinates.ON_BOARD[n]) {
						if (weights[p] < 0 && newweights[p] < 0) {
							if (weights[n] >= 0) {
								newweights[p]++;
							}
						}
						else if (weights[p] > 0 && newweights[p] > 0) {
							if (weights[n] <= 0) {
								newweights[p]--;
							}
						}
					}
				}
			}
			for (int i = 0; i < newweights.length; i++) {
				weights[i] = newweights[i];
			}
		}
	}

}
