package orego.heuristic;

import orego.core.Board;

/** Adjusts the probability of playing a move using domain-specific knowledge. */
public interface Heuristic {

	/**
	 * Returns a positive value if p is a good move for the current player on
	 * board, a negative value if it's bad.
	 */
	public int evaluate(int p, Board board);

}