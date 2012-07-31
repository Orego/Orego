package orego.heuristic;

import orego.core.Board;
import static orego.core.Coordinates.*;

/** Adjusts the probability of playing a move using domain-specific knowledge. */
public abstract class Heuristic {

	/**
	 * The weight given to the heuristic
	 */
	private int weight;

	public Heuristic(int weight) {
		this.weight = weight;
	}

	// TODO Make this abstract
	public int[] getSearchArea(Board board) {
		return ALL_POINTS_ON_BOARD;
	}
	
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	// TODO If a Heuristic is always associated with a particular Player or
	// McRunnable, do we need to pass the board around? Could we just pass in a
	// pointer at construction time?
	/**
	 * Called before any calls to evaluate on a given board state. For some
	 * heuristics, this avoids redundant computation. Does nothing by default.
	 */
	public void prepare(Board board) {
		// Does nothing
	}

	/**
	 * Returns a positive value if p is a good move for the current player on
	 * board, a negative value if it's bad.
	 */
	public abstract int evaluate(int p, Board board);

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

}