package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.ShortList;

/**
 * Remembers the sequence of moves played on this board (not including initial
 * stones).
 */
public class HistoryObserver implements BoardObserver {

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
		if (board.getTurn() > 0) {
			history.add(location);
		}
	}

	@Override
	public void clear() {
		history.clear();
	}
	
	/** Returns the move played at time t. */
	public short get(int t) {
		return history.get(t);
	}

	@Override
	public void copyDataFrom(BoardObserver that) {
		HistoryObserver original = (HistoryObserver)that;
		history.copyDataFrom(original.history);
	}

}
