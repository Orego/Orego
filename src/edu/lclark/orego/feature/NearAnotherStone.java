package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.BitBoard;
import edu.lclark.orego.util.ShortList;

/** True if p is "near" another stone, i.e., within Manhattan distance 4. */
public final class NearAnotherStone extends AbstractFeature implements BoardObserver {

	/** Points where stones have been played. */
	private final BitBoard playedPoints;
	
	/** Points near past stones, which are therefore reasonable places to play. */
	private final BitBoard nearbyPositions;
	
	public NearAnotherStone(Board board) {
		super(board);
		board.addObserver(this);
		playedPoints = new BitBoard(board.getCoordinateSystem());
		nearbyPositions = new BitBoard(board.getCoordinateSystem());
	}
	
	@Override
	public boolean at(short p){
		return nearbyPositions.get(p);
	}

	@Override
	public void update(StoneColor color, short location, ShortList capturedStones){
		playedPoints.set(location);
		nearbyPositions.copyDataFrom(playedPoints);
		for(int i = 0; i < 4; i++){
			nearbyPositions.expand();
		}
	}

}
