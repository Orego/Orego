package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.StoneColor;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.util.ShortList;

public class LastMoveObserver implements BoardObserver {

	private short lastMove;
	
	private final Board board;
	
	public LastMoveObserver(Board board){
		this.board = board;
		lastMove = NO_POINT;
		board.addObserver(this);
	}
	
	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		lastMove = location;
	}

	@Override
	public void clear() {
		lastMove = NO_POINT;
	}
	
	public short getLastMove(){
		return lastMove;
	}

	@Override
	public void copyDataFrom(BoardObserver that) {
		lastMove = ((LastMoveObserver) that).lastMove;
	}

}
