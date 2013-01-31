package orego.book;

import orego.core.Board;

/** A book which quickly finds good moves during the opening. */
public interface OpeningBook {

	/**
	 * Returns the next move in the opening book or NO_POINT if the book has
	 * been exhausted.
	 */
	public int nextMove(Board board);

}
