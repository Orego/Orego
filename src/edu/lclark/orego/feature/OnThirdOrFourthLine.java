package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;

/** True if p is on the third or fourth line. */
public final class OnThirdOrFourthLine implements Feature {
	
	private final Board board;
	
	public OnThirdOrFourthLine(Board board){
		this.board = board;
	}

	@Override
	public boolean at(short p){
		return board.getCoordinateSystem().isOnThirdOrFourthLine(p);
	}

	@Override
	public void clear() {
		// Does nothing
	}

}
