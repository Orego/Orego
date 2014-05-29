package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;

/**
 * Class for Features to extend, providing a field for the board and default do-nothing methods.
 */
public abstract class AbstractFeature implements Feature {

	private final Board board;
	
	public AbstractFeature(Board board) {
		this.board = board;
	}

	/** Returns the board associated with this feature. */
	protected Board getBoard() {
		return board;
	}

	@Override
	public void clear() {
		// Does nothing
	}

}
