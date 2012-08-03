package orego.heuristic;

import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.util.*;
import static orego.core.Coordinates.*;

/** Adjusts the probability of playing a move using domain-specific knowledge. */
public abstract class Heuristic {

	/** The move with the highest rating, or NO_POINT if there is no good move. */
	private int bestMove;

	// TODO Deal with the possibility of negative values;
	/** Moves given nonzero value by this heuristic. */
	private IntSet nonzeroPoints;

	/** Values of various moves. */
	private int[] values;

	/**
	 * The weight given to the heuristic
	 */
	private int weight;

	public Heuristic(int weight) {
		this.weight = weight;
		nonzeroPoints = new IntSet(FIRST_POINT_BEYOND_BOARD);
		values = new int[FIRST_POINT_BEYOND_BOARD];
	}

	public void setNonzeroPoints(IntSet nonzeroPoints) {
		this.nonzeroPoints = nonzeroPoints;
	}

	/**
	 * Returns a positive value if p is a good move for the current player on
	 * board, a negative value if it's bad.
	 */
	public int evaluate(int p, Board board) {
		return values[p];
	}

	/**
	 * Returns the move with the highest rating, or NO_POINT if there is no good
	 * move.
	 */
	public int getBestMove() {
		return bestMove;
	}

	/** Returns the set of moves given nonzero value by this heuristic. */
	public IntSet getNonzeroPoints() {
		return nonzeroPoints;
	}

	/** Returns the array of values. */
	protected int[] getValues() {
		return values;

	}

	public void setValues(int[] values) {
		this.values = values;
	}

	public int getWeight() {
		return weight;
	}

	/** Increases the value of point by amount, adjusting bestMove as appropriate. */
	protected void increaseValue(int point, int amount) {
		IntSet nonzeroPoints = getNonzeroPoints();
		if (nonzeroPoints.contains(point)) {
			values[point] += amount;
		} else {
			values[point] = amount;
			nonzeroPoints.add(point);
		}
		if ((bestMove == NO_POINT) || (values[point] > values[bestMove])) {
			bestMove = point;
		}
	}

	/**
	 * Called before any calls to evaluate on a given board state. For some
	 * heuristics, this avoids redundant computation. Overriding versions
	 * should first call this version.
	 * @param random TODO
	 */
	public void prepare(Board board, MersenneTwisterFast random) {
		nonzeroPoints.clear();
		bestMove = NO_POINT;
	}

	/**
	 * Sets the best move.
	 */
	protected void setBestMove(int bestMove) {
		this.bestMove = bestMove;
	}

	/**
	 * Allows external clients to optimize parameters. Subclasses should
	 * override if they have additional 'tunable' parameters.
	 * 
	 * @param property
	 *            The name of the property
	 * @param value
	 *            The value of the property
	 */
	public void setProperty(String property, String value) {
		if (property.equals("weight")) {
			this.weight = Integer.valueOf(value);
		}
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
