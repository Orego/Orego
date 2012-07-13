package orego.response;

import static orego.core.Coordinates.PASS;

import java.util.HashMap;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.McRunnable;
import orego.response.ResponsePlayer.TableLevel;
import sun.security.provider.certpath.OCSPResponse.ResponseStatus;
import ec.util.MersenneTwisterFast;

public class UctResponsePlayer extends ResponsePlayer {
	
	public UctResponsePlayer(){
		super();
		getResponses().remove(levelZeroEncodedIndex(Colors.BLACK));
		getResponses().remove(levelZeroEncodedIndex(Colors.WHITE));
	}
	
	@Override
	protected int findAppropriateMove(Board board, int history1, int history2, MersenneTwisterFast random, boolean isFinalMove) {

		int colorToPlay = board.getColorToPlay();
		
		// pick table based on threshold values
		AbstractResponseList list = getResponses().get(levelTwoEncodedIndex(history2, history1, colorToPlay));
		
		// make an entry if there wasn't one
		if(list == null) {
			list = new UctResponseList();
			getResponses().put(levelTwoEncodedIndex(history2, history1, colorToPlay), list);
		}
		
		// debugging
		if (isFinalMove) lastTableLevel = TableLevel.LevelTwo;
		
		return list.bestMove(board, random);
	
	}
	
	@Override
	protected synchronized void updateWins(int turn, int winner, Board board, int colorToPlay) {

		int prevMove = board.getMove(turn - 1);
		
		AbstractResponseList twoList = null;
		
		// we only track passes and most other moves in the two table.
		// We don't add the entry to the two table unless we've
		// seen it once and added it to the one level table
		// This saves space.
		int prevPrevMove 	= board.getMove(turn - 2);
		
		int twoKey = levelTwoEncodedIndex(prevPrevMove, prevMove, colorToPlay);
		twoList = getResponses().get(twoKey);
		
		if (twoList == null) { // if response list doesn't exist, create it
			twoList = new UctResponseList();
			getResponses().put(twoKey, twoList);
		}
	
		if (winner == colorToPlay) // we've won through this node
			twoList.addWin(board.getMove(turn));
		else
			twoList.addLoss(board.getMove(turn)); // we've lost through this node
	}
	
	@Override
	public void addWins(int move, Board board, int wins) {		
		int prevMove     = board.getMove(board.getTurn() - 1);
		int prevPrevMove = board.getMove(board.getTurn() - 2);
		int colorToPlay  = board.getColorToPlay();
		
		// get the second level table (create if it doesn't exist)
		int key = levelTwoEncodedIndex(prevPrevMove, prevPrevMove, colorToPlay);
		
		AbstractResponseList twoList = getResponses().get(key);

		if (twoList == null) {
			twoList = new UctResponseList();
			getResponses().put(key, twoList);
		}

		for(int i = 0; i < wins; i++) {
			twoList.addWin(move);
		}
	}
}
