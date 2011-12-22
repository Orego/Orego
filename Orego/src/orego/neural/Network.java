package orego.neural;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.LAST_POINT_ON_BOARD;
import orego.core.Board;
import ec.util.MersenneTwisterFast;

/** A neural network that evaluates moves based on recent history. */
public class Network {

	/**
	 * Index of the bias node. The bias is stored as if it were a weight from
	 * move BIAS at history 0.
	 */
	public static final int BIAS = 2;

	/**
	 * Activations of the hidden units.
	 */
	private double[] hiddenActivation;

	/** The number of previous moves to use as inputs. */
	private int history;

	/** The learning rate. */
	private final double learn;

	/**
	 * Weights from hidden layer to output layer. [color to play][hidden
	 * unit][output unit]
	 */
	private double[][][] weightsHiddenOutput;

	/**
	 * Weights from input layer to hidden layer. [color to play][input
	 * unit][depth of previous move][hidden unit]
	 */
	private double[][][][] weightsInputHidden;

	/**
	 * Weights from input directly to output without going through the hidden
	 * units. [color to play][input unit][depth of previous move][output unit]
	 */
	private double[][][][] weightsInputOutput;

	/**
	 * A neural network. Weights are created with random values between -.5 and
	 * .5
	 * 
	 * @param learn
	 *            the learning rate.
	 * @param hidden
	 *            the number of hidden units.
	 * @param history
	 *            how many previous moves to take into account. Must be
	 *            positive.
	 */
	public Network(double learn, int hidden, int history) {
		this.learn = learn;
		this.history = history;
		MersenneTwisterFast random = new MersenneTwisterFast();
		hiddenActivation = new double[hidden];
		weightsInputHidden = new double[2][LAST_POINT_ON_BOARD + 1][history][hidden];
		weightsHiddenOutput = new double[2][hidden][LAST_POINT_ON_BOARD + 1];
		weightsInputOutput = new double[2][LAST_POINT_ON_BOARD + 1][history][LAST_POINT_ON_BOARD + 1];
		// randomize input->hidden weights
		for (int i = 0; i < history; i++) {
			for (int j = 0; j <= LAST_POINT_ON_BOARD; j++) {
				for (int k = 0; k < hidden; k++) {
					weightsInputHidden[BLACK][j][i][k] = random.nextDouble() - .5;
					weightsInputHidden[WHITE][j][i][k] = random.nextDouble() - .5;
				}
			}
		}
		// randomize input->output weights
		for (int i = 0; i < history; i++) {
			for (int j = 0; j <= LAST_POINT_ON_BOARD; j++) {
				for (int k = 0; k <= LAST_POINT_ON_BOARD; k++) {
					weightsInputOutput[BLACK][j][i][k] = random.nextDouble() - .5;
					weightsInputOutput[WHITE][j][i][k] = random.nextDouble() - .5;
				}
			}
		}
		// randomize hidden->output weights
		for (int i = 0; i <= LAST_POINT_ON_BOARD; i++) {
			for (int k = 0; k < hidden; k++) {
				weightsHiddenOutput[BLACK][k][i] = random.nextDouble() - .5;
				weightsHiddenOutput[WHITE][k][i] = random.nextDouble() - .5;
			}
		}
	}

	/**
	 * Computes the activations of the hidden units.
	 * 
	 * @param color
	 *            the color to play.
	 * @param board
	 *            the board (which contains a history).
	 * @param turn
	 *            the turn to evaluate.
	 */
	protected void computeHiddenActivations(int color, Board board, int turn) {
		for (int j = 0; j < hiddenActivation.length; j++) {
			double x = weightsInputHidden[color][BIAS][0][j];
			for (int i = 0; i < history; i++) {
				x += weightsInputHidden[color][board.getMove(turn - i - 1)][i][j];
			}
			hiddenActivation[j] = squash(x);
		}
	}

	/**
	 * Calculates the activation of point.
	 * 
	 * @param color
	 *            the color to play.
	 * @param point
	 *            the point to evaluate.
	 * @param board
	 *            the board (which contains a history).
	 * @param turn
	 *            the turn to evaluate.
	 */
	public double evaluate(int color, int point, Board board, int turn) {
		computeHiddenActivations(color, board, turn);
		return evaluateFast(color, point, board, turn);
	}

	/**
	 * Similar to evaluate, but assumes computeHiddenActivations() has already
	 * been called. If evaluating many output points, this should be much
	 * faster.
	 * 
	 * @param color
	 *            the color to play.
	 * @param point
	 *            the point to evaluate.
	 * @param board
	 *            the board (which contains a history).
	 * @param turn
	 *            the turn to evaluate.
	 */
	public double evaluateFast(int color, int point, Board board, int turn) {
		// bias unit
		double sum = weightsInputOutput[color][2][0][point];
		for (int i = 0; i < history; i++) {
			sum += weightsInputOutput[color][board.getMove(turn - i - 1)][i][point];
		}
		for (int i = 0; i < hiddenActivation.length; i++) {
			sum += (hiddenActivation[i] * weightsHiddenOutput[color][i][point]);
		}
		return squash(sum);
	}

	/** Returns the weights from the hidden layer to the output layer. */
	protected double[][][] getWeightsHiddenOutput() {
		return weightsHiddenOutput;
	}

	/** Returns the weights from the input layer to the hidden layer. */
	protected double[][][][] getWeightsInputHidden() {
		return weightsInputHidden;
	}

	/**
	 * Returns the weights from input layer to output layer.
	 */
	protected double[][][][] getWeightsInputOutput() {
		return weightsInputOutput;
	}

	/**
	 * Changes the weights of the neural network.
	 * 
	 * @param color
	 *            the color to play.
	 * @param board
	 *            the board (which contains a history).
	 * @param turn
	 *            the turn.
	 * @param win
	 *            win or loss (1 or 0).
	 */
	public void learn(int color, Board board, int turn, int win) {
		int move = board.getMove(turn);
		double eval = evaluate(color, move, board, turn);
		double delta = (win - eval) * eval * (1 - eval);
		weightsInputOutput[color][2][0][move] += learn * delta;
		for (int i = 0; i < history; i++) {
			weightsInputOutput[color][board.getMove(turn - i - 1)][i][move] += learn
					* delta;
		}
		double[] delta2 = new double[hiddenActivation.length];
		for (int i = 0; i < hiddenActivation.length; i++) {
			delta2[i] = weightsHiddenOutput[color][i][move] * delta
					* hiddenActivation[i] * (1 - hiddenActivation[i]);
			weightsHiddenOutput[color][i][move] += learn * delta
					* hiddenActivation[i];
		}
		for (int i = 0; i < hiddenActivation.length; i++) {
			weightsInputHidden[color][2][0][i] += delta2[i]
					* hiddenActivation[i] * learn;
			for (int k = 0; k < history; k++) {
				weightsInputHidden[color][board.getMove(turn - k - 1)][k][i] += delta2[i]
						* hiddenActivation[i] * learn;
			}
		}
	}

	/** Logistic sigmoid squashing function. */
	protected double squash(double x) {
		return 1 / (1 + (Math.exp(-x)));
	}

}
