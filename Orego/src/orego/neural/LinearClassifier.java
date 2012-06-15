package orego.neural;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.*;
import ec.util.MersenneTwisterFast;

/** A linear perceptron that evaluates moves based on recent history. */
public class LinearClassifier {

	/** The number of previous moves to use as inputs. */
	private int history;

	protected int getHistory() {
		return history;
	}

	protected double getLearn() {
		return learn;
	}

	/** The learning rate. */
	private double learn;

	/**
	 * Index of the bias node.
	 * 
	 * @see #weights
	 */
	public static final int BIAS = 2;

	/**
	 * Weights in the classifier. Indices are [color to play][input
	 * point][history depth][output point]. For example,
	 * weights[BLACK][at("a1")][2][at("b1")] is the weight for black playing at
	 * b1 two moves after a play at a1. The bias weight for each output is
	 * stored as if it were from point BIAS at history 0.
	 */
	private double[][][][] weights;

	/**
	 * @param learn
	 *            learning rate.
	 * @param history
	 *            number of previous moves to take into account. Must be
	 *            positive.
	 */
	public LinearClassifier(double learn, int history) {
		this.learn = learn;
		this.history = history;
//		// MersenneTwisterFast random = new MersenneTwisterFast();
		weights = new double[2][FIRST_POINT_BEYOND_BOARD][history][FIRST_POINT_BEYOND_BOARD];
		for (int a = 0; a < weights.length; a++) {
			for (int b = 0; b < weights[a].length; b++) {
				for (int c = 0; c < weights[a][b].length; c++) {
					for (int d = 0; d < weights[a][b][c].length; d++) {
						weights[a][b][c][d] = 1.0;
					}
				}
			}
		}
//		for (int i = 0; i < FIRST_POINT_BEYOND_BOARD; i++) {
//			weights[BLACK][BIAS][0][i] = initialWeight(i, at("e5"));
//			weights[WHITE][BIAS][0][i] = initialWeight(i, at("e5"));
//		}
//		// // Initialize weights randomly
//		for (int k : ALL_POINTS_ON_BOARD) {
//			for (int i = 0; i < this.history; i++) {
//				for (int j : ALL_POINTS_ON_BOARD) {
//					weights[BLACK][j][i][k] = initialWeight(j, k);
//					weights[WHITE][j][i][k] = initialWeight(j, k);
//				}
//				weights[BLACK][NO_POINT][i][k] = initialWeight(k, k);
//				weights[WHITE][NO_POINT][i][k] = initialWeight(k, k);				
//			}
//		}
	}

	protected double initialWeight(int p, int q) {
		return (1.0 / 3)
		- (getDistance(p, q) * getDistance(p, q)) / (144 * 3);
	}

	/**
	 * Returns the estimated value of point. Ideally this is 1 for winning moves
	 * and 0 for losing moves, but it is not bound to be in this range.
	 * 
	 * @param color
	 *            the color to play.
	 * @param point
	 *            the point being considered.
	 * @param board
	 *            the board.
	 * @param turn
	 *            the turn at which the move is to be played. May be less than
	 *            board.getTurn(), e.g., when evaluating a past move for
	 *            learning.
	 */
	public double evaluate(int color, int point, Board board, int turn) {
		double sum = weights[color][BIAS][0][point];
		for (int i = 0; i < history; i++) {
			sum += weights[color][board.getMove(turn - i - 1)][i][point];
		}
		return sum;
	}

	/**
	 * Returns the weights.
	 */
	protected double[][][][] getWeights() {
		return weights;
	}

	/**
	 * Takes the color to play, the board, the turn, and whether the move at
	 * turn was a win for that color (0 or 1). Changes the weights of the
	 * classifier.
	 */
	public void learn(int color, Board board, int turn, int win) {
		int move = board.getMove(turn);
		double eval = evaluate(color, move, board, turn);
		double error = (win - eval);
		double change = learn * error;
		weights[color][BIAS][0][move] += change;
//		System.out.println(pointToString(board.getMove(turn - 2)) + ", " + pointToString(board.getMove(turn - 1)) + " -> "
//				+ colorToString(color) + pointToString(board.getMove(turn)));
		for (int i = 0; i < history; i++) {
			weights[color][board.getMove(turn - i - 1)][i][move] += change;
		}
	}

	public static void main(String[] args) {
		LinearClassifier f = new LinearClassifier(.1, 2);
	}

}
