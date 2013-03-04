package orego.neural;

import orego.core.Board;

public class AverageClassifier extends LinearClassifier {
	
	public AverageClassifier(double learn, int history) {
		super(learn, history);
		double[][][][] weights = getWeights();
		for (int a = 0; a < weights.length; a++) {
			for (int b = 0; b < weights[a].length; b++) {
				for (int c = 0; c < weights[a][b].length; c++) {
					for (int d = 0; d < weights[a][b][c].length; d++) {
						weights[a][b][c][d] = 1.0;
					}
				}
			}
		}
	}

	@Override
	public double evaluate(int color, int point, Board board, int turn) {
		double sum = getWeights()[color][BIAS][0][point];
		// for (int i = 0; i < getHistory(); i++) {
		// sum += 0.45 * getWeights()[color][board.getMove(turn - i -
		// 1)][i][point];
		// }
		sum += getWeights()[color][board.getMove(turn - 1)][0][point]; // Previous
		sum += getWeights()[color][board.getMove(turn - 2)][1][point]; // Penultimate
		return sum/(getHistory() + 1); // / (getHistory() + 1);
	}

	@Override
	public void learn(int color, Board board, int turn, int win) {
		int move = board.getMove(turn);
		getWeights()[color][BIAS][0][move] += getLearn()
				* (win - getWeights()[color][BIAS][0][move]);
		// System.out.println(pointToString(board.getMove(turn - 2)) + ", " +
		// pointToString(board.getMove(turn - 1)) + " -> "
		// + colorToString(color) + pointToString(board.getMove(turn)));
		for (int i = 0; i < getHistory(); i++) {
			getWeights()[color][board.getMove(turn - i - 1)][i][move] += getLearn()
					* (win - getWeights()[color][board.getMove(turn - i - 1)][i][move]);
		}
	}
}
