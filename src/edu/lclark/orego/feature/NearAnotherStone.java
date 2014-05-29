package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.BitBoard;

public final class NearAnotherStone implements Feature{

	private final Board board;
	
	private final BitBoard bitBoard;

	public NearAnotherStone(Board board) {
		this.board = board;
		bitBoard = new BitBoard(board.getCoordinateSystem());
	}
	
	@Override
	public boolean at(short p){
		return bitBoard.get(p);
	}
	
	@Override
	public void clear() {
		// Does nothing
	}

}
