package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.CoordinateSystem.*;

/** Suggests good replies stores in a Last Good Reply table. */
@SuppressWarnings("serial")
public final class LgrfSuggester implements Suggester {
	
	private final HistoryObserver history;
	
	private final LgrfTable table;
	
	private final Board board;
	
	private final ShortSet moves;
	
	public LgrfSuggester(Board board, HistoryObserver history, LgrfTable table){
		this.board = board;
		this.history = history;
		this.table = table;
		moves = new ShortSet(board.getCoordinateSystem().getFirstPointBeyondBoard());
	}

	@Override
	public ShortSet getMoves() {
		moves.clear();
		short previousMove = history.get(board.getTurn()-1);
		short reply = table.getSecondLevelReply(board.getColorToPlay(), history.get(board.getTurn()-2), previousMove);
		if(reply != NO_POINT){
			moves.add(reply);
			System.out.println("Suggesting 2nd level");
			return moves;
		}
		reply = table.getFirstLevelReply(board.getColorToPlay(), previousMove);
		if(reply != NO_POINT){
			moves.add(reply);
			System.out.println("Suggesting 1st level");
			return moves;
		}
		return moves;
	}

}
