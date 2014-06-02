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

	/** The sequence of moves. */
	private final ShortList history;
	
	public HistoryObserver(Board board) {
		CoordinateSystem coords = board.getCoordinateSystem();
		history = new ShortList(coords.getMaxMovesPerGame());
	}

	// TODO We'll need clear and copyDataFrom
	// The latter will need to take a new board as an argument
	
	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		history.add(location);
	}

	/** Returns the move played at time t. */
	public short get(int t) {
		return history.get(t);
	}

}
