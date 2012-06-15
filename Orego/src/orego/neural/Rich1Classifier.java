package orego.neural;

import static orego.core.Coordinates.*;
import orego.core.Board;

public class Rich1Classifier {

	/**
	 * Index of the bias node.
	 * 
	 * @see #weights
	 */
	public static final int BIAS = 2;

	/** The number of previous moves to use as inputs. */
	private int history;

	/** The learning rate. */
	private double learn;

	/**
	 * Weights in the classifier. Indices are [color to play][input
	 * point][history depth][output point]. For example,
	 * weights[BLACK][at("a1")][2][at("b1")] is the weight for black playing at
	 * b1 two moves after a play at a1. The bias weight for each output is
	 * stored as if it were from point BIAS at history 0.
	 */
	private double[][][][] weights;

	/**
	 * [color to play][point][neighbor #][color]
	 */
	private double[][][][] neighbors;

	protected double[][][][] getNeighbors() {
		return neighbors;
	}

	protected void setNeighbors(double[][][][] neighbors) {
		this.neighbors = neighbors;
	}

	/**
	 * @param learn
	 *            learning rate.
	 * @param history
	 *            number of previous moves to take into account. Must be
	 *            positive.
	 */
	public Rich1Classifier(double learn, int history) {
		this.learn = learn;
		this.history = history;
		// // MersenneTwisterFast random = new MersenneTwisterFast();
		weights = new double[2][FIRST_POINT_BEYOND_BOARD][history][FIRST_POINT_BEYOND_BOARD];
		neighbors = new double[2][FIRST_POINT_BEYOND_BOARD][ALL_POINTS_ON_BOARD.length][4];
		for (int a = 0; a < weights.length; a++) {
			for (int b = 0; b < weights[a].length; b++) {
				for (int c = 0; c < weights[a][b].length; c++) {
					for (int d = 0; d < weights[a][b][c].length; d++) {
						weights[a][b][c][d] = 1.0;
					}
				}
			}
		}
		for (int d = 0; d < 2; d++) {
			for (int a = 0; a < neighbors[d].length; a++) {
				for (int b = 0; b < neighbors[d][a].length; b++) {
					for (int c = 0; c < neighbors[d][a][b].length; c++) {
						neighbors[d][a][b][c] = 1.0;
					}
				}
			}
		}
	}

	public double evaluate(int color, int point, Board board, int turn) {
		double sum = getWeights()[color][BIAS][0][point];
		int[] colorsAround;
		if (turn == board.getTurn()) {
			colorsAround = board.getColorsAround(point);
		} else {
			colorsAround = board.getHistoricalColorsAround(turn);
		}
		for (int i = 0; i < getHistory(); i++) {
			sum += getWeights()[color][board.getMove(turn - i - 1)][i][point];
		}
		for (int i = 0; i < colorsAround.length; i++) {
			sum += neighbors[color][point][i][colorsAround[i]];
		}
		return sum
				/ (history + 1 + colorsAround.length);
	}

	protected int getHistory() {
		return history;
	}

	protected double getLearn() {
		return learn;
	}

	/**
	 * Returns the weights.
	 */
	protected double[][][][] getWeights() {
		return weights;
	}

	public void learn(int color, Board board, int turn, int win) {
		int move = board.getMove(turn);
		weights[color][BIAS][0][move] += getLearn()
				* (win - weights[color][BIAS][0][move]);
		// System.out.println(pointToString(board.getMove(turn - 2)) + ", " +
		// pointToString(board.getMove(turn - 1)) + " -> "
		// + colorToString(color) + pointToString(board.getMove(turn)));
		for (int i = 0; i < getHistory(); i++) {
			weights[color][board.getMove(turn - i - 1)][i][move] += getLearn()
					* (win - weights[color][board.getMove(turn - i - 1)][i][move]);
		}
		if (move != PASS) {
			int[] colorsAround = board.getHistoricalColorsAround(turn);
			for (int k = 0; k < colorsAround.length; k++) {
				neighbors[color][move][k][colorsAround[k]] += getLearn()
						* (win - neighbors[color][move][k][colorsAround[k]]);
			}
		}
	}

}
