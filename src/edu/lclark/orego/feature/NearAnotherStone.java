package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.BitBoard;
import edu.lclark.orego.util.ShortList;

public final class NearAnotherStone extends AbstractFeature implements BoardObserver {

	private final BitBoard boardState;
	
	private final BitBoard nearbyPositions;
	
	public NearAnotherStone(Board board) {
		super(board);
		board.addObserver(this);
		boardState = new BitBoard(board.getCoordinateSystem());
		nearbyPositions = new BitBoard(board.getCoordinateSystem());
	}
	
	@Override
	public boolean at(short p){
		return nearbyPositions.get(p);
	}

	@Override
	public void update(StoneColor color, short location, ShortList capturedStones){
		boardState.set(location);
		nearbyPositions.copyDataFrom(boardState);
		for(int i = 0; i < 4; i++){
			nearbyPositions.expand();
		}
	}
}
