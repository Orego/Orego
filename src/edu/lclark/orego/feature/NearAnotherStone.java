package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;

public final class NearAnotherStone implements Feature{

	private final Board board;

	public NearAnotherStone(Board board) {
		this.board = board;
	}
	
	@Override
	public boolean at(short p){
		return false;
	}
	
	@Override
	public void clear() {
		// Does nothing
	}

}
