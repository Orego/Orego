package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.EXTENDED_BOARD_AREA;
import static orego.core.Coordinates.NEIGHBORS;
import orego.core.Board;
import orego.core.Coordinates;

public class TerritoryHeuristic extends Heuristic {
	
	public TerritoryHeuristic(double weight) {
		setWeight(weight);
	}
	
	int[] weights;

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
	
	@Override
	public int evaluate(int p, Board board) {
		calculateTerritory(board);
		if (weights[p] != 0) {
			return -1;
		}
		else {
			return 0;
		}
	}

}
