package orego.mcts;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;

import java.util.HashMap;
import java.util.Hashtable;

import orego.core.Board;
import orego.core.Coordinates;
import orego.heuristic.HeuristicList;
import ec.util.MersenneTwisterFast;

public class WLSMcRunnable extends McRunnable {
	/** A *reference* to the WLSPlayer's copy of the level two response table*/
	private HashMap<Integer, WLSResponseMoveList> bestReplies;
	
	
	public WLSMcRunnable(McPlayer player, HeuristicList heuristics, HashMap<Integer, WLSResponseMoveList> bestReplies) {
		super(player, heuristics);
		
		this.bestReplies = bestReplies;
	}
	
	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int antepenultimate = board.getMove(board.getTurn() - 2);
		int previous 	    = board.getMove(board.getTurn() - 1);
		WLSResponseMoveList list = bestReplies.get(WLSPlayer.levelTwoEncodedIndex(antepenultimate, previous, board.getColorToPlay()));
		
		if (list != null) {
			// work through our best moves (from best to worst) and test legality
			
			// cache local variables
			int[] topResponses = list.getTopResponses();
			int kTopResponses = topResponses.length;
			
			for (int i = 0; i < kTopResponses; i++) {
				int responseMove = topResponses[i];
				
				if (responseMove == Coordinates.NO_POINT) {
					continue;
				}
				
				// Try a level 2 reply and return the first legal move
				if ((board.getColor(responseMove) == VACANT) && 
				     board.isFeasible(responseMove) 		 && 
				     (board.playFast(responseMove) == PLAY_OK)) {
					// clear the illegality counter
					list.clearIllegality(i);
					
					return responseMove;
				} else {
					// note the move at index i is illegal
					list.addIllegalPlay(i);
				}
			}
		}
		
		// No good replies stored; proceed normally
		return super.selectAndPlayOneMove(random, board);
	}
}
