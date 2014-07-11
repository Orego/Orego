package edu.lclark.orego.book;

import edu.lclark.orego.core.Board;

public interface OpeningBook {

	/** Returns the stored response to a given board state. */
	public short nextMove(Board board);

}
