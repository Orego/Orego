package tictactoe;

import java.util.LinkedList;

public class MinmaxPlayer {
	public int min(Board board) {

		if (board.isOver()) {
			if (board.getWinner() == 2) {
				return -1;
			}
			return board.getWinner();
		}

		int min= 100;
		int n = 100;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board.isVacant(i, j)) {
					Board temp = board.copy();
					temp.play(i, j, board.getCurrentPlayer());
					temp.switchPlayers();
					n = max(temp);
					if (n < min) {
						min = n;
					}
				}
			}
		}
		return min;

	}

	public int max(Board board) {

		if (board.isOver()) {
			if (board.getWinner() == 2) {
				return -1;
			}
			return board.getWinner();
		}

		int max = -100;
		int n = -100;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board.isVacant(i, j)) {
					Board temp = board.copy();
					temp.play(i, j, board.getCurrentPlayer());
					temp.switchPlayers();
					n = min(temp);
					if (n > max) {
						max = n;
					}
				}
			}
		}
		return max;

	}

	public int minmax(Board board) {
		int bestRow = -1;
		int bestColumn = -1;
		int best;
		int current;
		if (board.getCurrentPlayer() == 1) {
			best = -101;
		} else {
			best = 101;
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board.isVacant(i, j)) {
					Board temp = board.copy();

					temp.play(i, j, board.getCurrentPlayer());
					temp.switchPlayers();
					if (board.getCurrentPlayer() == 1) {
						current = min(temp);
						if (best < current) {
							best = current;
							bestRow = i;
							bestColumn = j;
						}

					} else {
						current = max(temp);
						if (best > current) {
							best = current;
							bestRow = i;
							bestColumn = j;
						}

					}

				}
			}
		}
		return bestRow * 3 + bestColumn;
	}
}
