package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.ShortSet;

/** Suggests good replies stores in a Last Good Reply table. */
@SuppressWarnings("serial")
public final class LgrfSuggester implements Suggester {

	private final Board board;

	private final HistoryObserver history;

	private final int bias;
	
	private final ShortSet moves;

	/**
	 * The table is transient because we want the LgrfSuggesters in all
	 * McRunnables to share the same table.
	 */
	private transient LgrfTable table;
	
	public LgrfSuggester(Board board, HistoryObserver history, LgrfTable table){
		this(board, history, table, 0);
	}

	public LgrfSuggester(Board board, HistoryObserver history, LgrfTable table, int bias) {
		this.bias = bias;
		this.board = board;
		this.history = history;
		this.table = table;
		moves = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
	}

	@Override
	public ShortSet getMoves() {
		moves.clear();
		final short previousMove = history.get(board.getTurn() - 1);
		short reply = table.getSecondLevelReply(board.getColorToPlay(),
				history.get(board.getTurn() - 2), previousMove);
		if (reply != NO_POINT && board.getColorAt(reply) == VACANT) {
			moves.add(reply);
			return moves;
		}
		reply = table.getFirstLevelReply(board.getColorToPlay(), previousMove);
		if (reply != NO_POINT && board.getColorAt(reply) == VACANT) {
			moves.add(reply);
			return moves;
		}
		return moves;
	}

	public void setTable(LgrfTable table) {
		this.table = table;
	}

	@Override
	public int getBias() {
		return bias;
	}

}
