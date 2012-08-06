package orego.heuristic;

import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.play.UnknownPropertyException;
import orego.util.*;
import static orego.core.Coordinates.*;

/** Adjusts the probability of playing a move using domain-specific knowledge. */
public abstract class Heuristic {

	private IntSet goodMoves;

	private IntSet badMoves;
	
	/**
	 * The weight given to the heuristic
	 */
	private int weight;

	public Heuristic(int weight) {
		this.weight = weight;
		goodMoves = new IntSet(FIRST_POINT_BEYOND_BOARD);
		badMoves = new IntSet(FIRST_POINT_BEYOND_BOARD);
	}


	public IntSet getGoodMoves() {
		return goodMoves;
	}


	protected void setGoodMoves(IntSet goodMoves) {
		this.goodMoves = goodMoves;
	}


	public IntSet getBadMoves() {
		return badMoves;
	}


	protected void setBadMoves(IntSet badMoves) {
		this.badMoves = badMoves;
	}

	protected void recommend(int p) {
		goodMoves.add(p);
	}
	
	protected void discourage(int p) {
		badMoves.add(p);
	}

	public int getWeight() {
		return weight;
	}

	/**
	 * Called before any calls to evaluate on a given board state. For some
	 * heuristics, this avoids redundant computation. Overriding versions
	 * should usually call this version first, as it clears out goodMoves
	 * and badMoves.
	 */
	public void prepare(Board board) {
		goodMoves.clear();
		badMoves.clear();
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
	public void setProperty(String property, String value) throws UnknownPropertyException {
		if (property.equals("weight")) {
			this.weight = Integer.valueOf(value);
		} else {
			throw new UnknownPropertyException("No property exists for '"
					+ property + "'");
		}
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
