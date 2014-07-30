package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.ShortSet;

/** Suggests good replies stores in a Last Good Reply table. */
@SuppressWarnings("serial")
public final class LgrfSuggester implements Suggester {

	private final int bias;

	private final Board board;

	private final HistoryObserver history;
	
	private final ShortSet moves;

	private final Predicate filter;
	
	/**
	 * The table is transient because we want the LgrfSuggesters in all
	 * McRunnables to share the same table.
	 */
	private transient LgrfTable table;
	
	public LgrfSuggester(Board board, HistoryObserver history, LgrfTable table, Predicate filter){
		this(board, history, table, 0, filter);
	}

	public LgrfSuggester(Board board, HistoryObserver history, LgrfTable table, int bias, Predicate filter) {
		this.bias = bias;
		this.board = board;
		this.history = history;
		this.table = table;
		this.filter = filter;
		moves = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
	}

	@Override
	public int getBias() {
		return bias;
	}

	@Override
	public ShortSet getMoves() {
		moves.clear();
		final short previousMove = history.get(board.getTurn() - 1);
		short reply = table.getSecondLevelReply(board.getColorToPlay(),
				history.get(board.getTurn() - 2), previousMove);
		if (reply != NO_POINT && board.getColorAt(reply) == VACANT && filter.at(reply)) {
			moves.add(reply);
		} else {
			reply = table.getFirstLevelReply(board.getColorToPlay(), previousMove);
			if (reply != NO_POINT && board.getColorAt(reply) == VACANT && filter.at(reply)) {
				moves.add(reply);
			}
		}
		return moves;
	}

	public void setTable(LgrfTable table) {
		this.table = table;
	}

}
