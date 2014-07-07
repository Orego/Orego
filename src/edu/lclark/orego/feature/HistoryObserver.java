package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.ShortList;

/**
 * Remembers the sequence of moves played on this board (not including initial
 * stones).
 */
@SuppressWarnings("serial")
public final class HistoryObserver implements BoardObserver {

	private final Board board;
		
	/** The sequence of moves. */
	private final ShortList history;
	
	public HistoryObserver(Board board) {
		this.board = board;
		CoordinateSystem coords = board.getCoordinateSystem();
		history = new ShortList(coords.getMaxMovesPerGame());
		board.addObserver(this);
	}

	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		assert location == CoordinateSystem.PASS || board.getCoordinateSystem().isOnBoard(location);
		if (board.getTurn() > 0) {
			history.add(location);
		}
	}

	@Override
	public void clear() {
		history.clear();
	}
	
	/** Returns the move played at time t. If t < 0, returns NO_POINT. */
	public short get(int t) {
		if (t < 0) {
			return NO_POINT;
		}
		return history.get(t);
	}

	@Override
	public void copyDataFrom(BoardObserver that) {
		HistoryObserver original = (HistoryObserver)that;
		history.copyDataFrom(original.history);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		CoordinateSystem coords = board.getCoordinateSystem();
		result.append("[");
		if (history.size() > 0) {
			result.append(coords.toString(history.get(0)));
			for (int t = 1; t < history.size(); t++) {
				result.append(", ");
				result.append(coords.toString(history.get(t)));
			}
		}
		result.append("]");
		return result.toString();
	}

}
