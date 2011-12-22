package orego.neural;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Math.log;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static orego.core.Coordinates.*;
import orego.core.Board;
import static orego.core.Colors.*;

public class RatioClassifier {

	/** The number of previous moves to use as inputs. */
	private int history;

	protected static int WINS = 0;

	protected static int RUNS = 1;

	public static final int BIAS = 2;

	/**
	 * Weights in the classifier. Indices are [color to play][input
	 * point][history depth][output point][wins or runs]. For example,
	 * weights[BLACK][at("a1")][1][at("b1")][WINS] is the number of wins for black playing at
	 * b1 two moves after a play at a1. The bias counts for each output are
	 * stored as if they were from point BIAS at history 0.
	 */
	private int[][][][][] counts;

	public RatioClassifier(int history) {
		this.history = history;
		counts = new int[2][LAST_POINT_ON_BOARD + 1][history][LAST_POINT_ON_BOARD + 1][2];
		for (int color = BLACK; color <= WHITE; color++) {
			for (int from = 0; from <= LAST_POINT_ON_BOARD; from++) {
				for (int h = 0; h < history; h++) {
					for (int to : ALL_POINTS_ON_BOARD) {
						counts[color][from][h][to][WINS] = 1;
						counts[color][from][h][to][RUNS] = 2;
					}
				}
			}
		}
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
		double sum = ((double)counts[color][BIAS][0][point][WINS])/counts[color][BIAS][0][point][RUNS];
		for (int i = 0; i < history; i++) {
			sum += ((double)counts[color][board.getMove(turn - i - 1)][i][point][WINS])
					/ counts[color][board.getMove(turn - i - 1)][i][point][RUNS];
		}
		return sum / (history + 1);
	}

	public double getUctValue(int color, int point, Board board, int turn) {
		int totalRunsFromHere = 0;
		for (int p : ALL_POINTS_ON_BOARD) {
			totalRunsFromHere += counts[color][BIAS][0][p][RUNS];
		}
		double sum = getUctValue(totalRunsFromHere, counts[color][BIAS][0][point]);
		for (int i = 0; i < history; i++) {
			totalRunsFromHere = 0;
			for (int p : ALL_POINTS_ON_BOARD) {
				totalRunsFromHere += counts[color][board.getMove(turn - i - 1)][i][p][RUNS];
			}
			sum += getUctValue(totalRunsFromHere, counts[color][board.getMove(turn - i - 1)][i][point]);
		}
		return sum / (history + 1);
	}

	/**
	 * Returns the UCT upper bound for node. This is the UCB1-TUNED policy,
	 * explained in the tech report by Gelly, et al, "Modification of UCT with
	 * Patterns in Monte-Carlo Go". The formula is at the bottom of p. 5 in that
	 * paper.
	 */
	public double getUctValue(int totalRunsFromHere, int[] winsAndRuns) {
		// The variable names here are chosen for consistency with the tech
		// report
		double barX = ((double)winsAndRuns[WINS]) / winsAndRuns[RUNS];
		if (barX < 0) { // if the move has been excluded
			return NEGATIVE_INFINITY;
		}
		double logParentRunCount = log(totalRunsFromHere);
		// In the paper, term1 is the mean of the SQUARES of the rewards; since
		// all rewards are 0 or 1 here, this is equivalent to the mean of the
		// rewards, i.e., the win rate.
		double term1 = barX;
		double term2 = -(barX * barX);
		double term3 = sqrt(2 * logParentRunCount / winsAndRuns[RUNS]);
		double v = term1 + term2 + term3; // This equation is above Eq. 1
		assert v >= 0 : "Negative variability in UCT";
		double factor1 = logParentRunCount / winsAndRuns[RUNS];
		double factor2 = min(0.25, v);
		double uncertainty = 0.4 * sqrt(factor1 * factor2);
		return uncertainty + barX;
	}

	/**
	 * Returns the weights.
	 */
	protected int[][][][][] getCounts() {
		return counts;
	}

	/**
	 * Takes the color to play, the board, the turn, and whether the move at
	 * turn was a win for that color (0 or 1). Changes the weights of the
	 * classifier.
	 */
	public void learn(int color, Board board, int turn, int win) {
		int move = board.getMove(turn);
		counts[color][BIAS][0][move][WINS] += win;
		counts[color][BIAS][0][move][RUNS] += 1;
		for (int i = 0; i < history; i++) {
			counts[color][board.getMove(turn - i - 1)][i][move][WINS] += win;
			counts[color][board.getMove(turn - i - 1)][i][move][RUNS] += 1;
		}
	}

}
