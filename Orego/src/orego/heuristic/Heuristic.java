package orego.heuristic;

import orego.core.Board;
import orego.util.*;
import static orego.core.Coordinates.*;

// TODO Test!
/** Adjusts the probability of playing a move using domain-specific knowledge. */
public abstract class Heuristic {

	/** The index, in nonzeroPoints, of the move with the highest rating. */
	private int bestIndex;
	
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
	
	/** Returns the index, in nonzeroPoints, of the move with the highest rating. */
	public int getBestIndex() {
		return bestIndex;
	}

	/** Returns the set of moves given nonzero value by this heuristic. */
	public IntSet getNonzeroPoints() {
		return nonzeroPoints;
	}
	
	public int[] getValues() {
		return values;
		
	}
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * Called before any calls to evaluate on a given board state. For some
	 * heuristics, this avoids redundant computation. Does nothing by default.
	 * 
	 * @param greedy If true, stop after finding one good move.
	 */
	public void prepare(Board board, boolean greedy) {
		nonzeroPoints.clear();
		bestIndex = -1;
	}

	/**
	 * Returns a positive value if p is a good move for the current player on
	 * board, a negative value if it's bad.
	 */
	public int evaluate(int p, Board board) {
		return values[p];
	}

	public void setBestIndex(int bestIndex) {
		this.bestIndex = bestIndex;
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

}