package orego.heuristic;

import orego.core.Board;
import orego.play.UnknownPropertyException;
import orego.util.*;
import static orego.core.Coordinates.*;

/** Adjusts the probability of playing a move using domain-specific knowledge. */
public abstract class Heuristic implements Cloneable {

	/**
	 * @see #getGoodMoves()
	 */
	private IntSet goodMoves;

	/**
	 * @see #getWeight()
	 */
	private int weight;

	public Heuristic(int weight) {
		this.weight = weight;
		goodMoves = new IntSet(getFirstPointBeyondBoard());
	}

	@Override
	public Heuristic clone() {
		Heuristic clone = null;
		try {
			clone = (Heuristic) super.clone();
			clone.weight = weight;
			clone.goodMoves = new IntSet(getFirstPointBeyondBoard());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return clone;
	}

	/** Returns the set of moves recommended by this heuristic. */
	public IntSet getGoodMoves() {
		return goodMoves;
	}

	/**
	 * Returns the weight of this heuristic (a non-negative integer, with higher
	 * weights making the heuristic more important).
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Called before any calls to evaluate on a given board state. For some
	 * heuristics, this avoids redundant computation. Overriding versions should
	 * usually call this version first, as it clears out goodMoves.
	 */
	public void prepare(Board board, boolean local, int treeDepth) {
		goodMoves.clear();
	}

	/** Adds p to the set of moves recommended by this heuristic. */
	protected void recommend(int p) {
		goodMoves.add(p);
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
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("weight")) {
			this.weight = Integer.valueOf(value);
		} else {
			throw new UnknownPropertyException("No property exists for '"
					+ property + "'");
		}
	}

}
