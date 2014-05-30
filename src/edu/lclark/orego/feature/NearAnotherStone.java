package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.BitBoard;

public final class NearAnotherStone extends AbstractFeature {

	private final BitBoard bitBoard;
	
	public NearAnotherStone(Board board) {
		super(board);
		bitBoard = new BitBoard(board.getCoordinateSystem());
	}
	
	@Override
	public boolean at(short p){
		return bitBoard.get(p);
	}

}
