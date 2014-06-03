package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.BitBoard;
import edu.lclark.orego.util.ShortList;

/** True if p is "near" another stone, i.e., within Manhattan distance 4. */
public final class NearAnotherStone implements Feature, BoardObserver {

	/** Points where stones have been played. */
	private final BitBoard playedPoints;

	/** Points near past stones, which are therefore reasonable places to play. */
	private final BitBoard nearbyPositions;

	/** True if all points are near another stone. */
	private boolean full;

	public NearAnotherStone(Board board) {
		board.addObserver(this);
		playedPoints = new BitBoard(board.getCoordinateSystem());
		nearbyPositions = new BitBoard(board.getCoordinateSystem());
	}

	@Override
	public boolean at(short p) {
		return full || nearbyPositions.get(p);
	}

	@Override
	public void clear() {
		full = false;
		playedPoints.clear();
		nearbyPositions.clear();
	}

	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		if (!full) {
			playedPoints.set(location);
			nearbyPositions.copyDataFrom(playedPoints);
			for (int i = 0; i < 4; i++) {
				nearbyPositions.expand();
			}
			full = nearbyPositions.isFull();
		}
	}

}
