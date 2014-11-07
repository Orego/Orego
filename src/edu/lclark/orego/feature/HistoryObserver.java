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
		final CoordinateSystem coords = board.getCoordinateSystem();
		history = new ShortList(coords.getMaxMovesPerGame());
		board.addObserver(this);
	}

	/**
	 * Produces a HistoryObserver that doesn't actually observe any particular
	 * board. It is useful for copying data from some other HistoryObserver.
	 */
	public HistoryObserver(CoordinateSystem coords) {
		board = null;
		history = new ShortList(coords.getMaxMovesPerGame());
	}

	@Override
	public void clear() {
		history.clear();
	}

	@Override
	public void copyDataFrom(BoardObserver that) {
		final HistoryObserver original = (HistoryObserver) that;
		history.copyDataFrom(original.history);
	}

	/** Returns the move played at time t. If t < 0, returns NO_POINT. */
	public short get(int t) {
		if (t < 0) {
			return NO_POINT;
		}
		return history.get(t);
	}

	public int size() {
		return history.size();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final CoordinateSystem coords = board.getCoordinateSystem();
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

	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		assert location == CoordinateSystem.PASS
				|| board.getCoordinateSystem().isOnBoard(location);
		if (board.getTurn() > 0) {
			history.add(location);
		}
	}

}
