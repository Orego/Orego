package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.CoordinateSystem.*;

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
		if(board.getTurn()<2){
			return moves;
		}
		short move = history.get(board.getTurn()-1);
		short reply = table.getSecondLevelReply(board.getColorToPlay(), history.get(board.getTurn()-2), move);
		if(reply != NO_POINT){
			moves.add(reply);
			return moves;
		}
		reply = table.getFirstLevelReply(board.getColorToPlay(), move);
		if(reply != NO_POINT){
			moves.add(reply);
			System.out.println("Suggesting 1st level");
			return moves;
		}
		return moves;
	}

}
