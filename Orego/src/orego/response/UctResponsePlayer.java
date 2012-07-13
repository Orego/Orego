package orego.response;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.McRunnable;
import ec.util.MersenneTwisterFast;

public class UctResponsePlayer extends ResponsePlayer {
	
	public UctResponsePlayer(){
		super();
		getResponses().remove(levelZeroEncodedIndex(Colors.BLACK));
		getResponses().remove(levelZeroEncodedIndex(Colors.WHITE));
	}
	
	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		MersenneTwisterFast random = runnable.getRandom();
		Board board = runnable.getBoard();
		int move;
		int history1;
		int history2;
		board.copyDataFrom(getBoard());
		int playoutLevel = 0;
		// TODO We'll be doing this differently in the future
		runnable.getPolicy().updateResponses(this, board, priorsWeight);
		// do a playout until the game finishes or we hit the max playout depth
		while (board.getPasses() < 2 && playoutLevel < MAX_PLAYOUT_DEPTH) {
			playoutLevel++; 
			history1 = board.getMove(board.getTurn() - 1);
			history2 = board.getMove(board.getTurn() - 2);
			
			// if the list has never been updated, get a move from the policy
			AbstractResponseList list = getResponses().get(levelTwoEncodedIndex(history2, history1, board.getColorToPlay()));
			if(list == null || list.getTotalRuns()
					// TODO Move this magic number elsewhere
					< 10 * orego.core.Coordinates.BOARD_AREA
//					== AbstractResponseList.NORMAL_RUNS_PRIOR * Coordinates.BOARD_AREA
					) {
				getPolicy().selectAndPlayOneMove(random, board);
				continue;
			}
			move = findAppropriateMove(board, history1, history2, random, false);
			runnable.acceptMove(move);
		}
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
		
		return list.bestMove(board, random);
	}
	
	@Override
	protected synchronized void updateWins(int turn, int winner, Board board, int colorToPlay) {
		int prevPrevMove 	= board.getMove(turn - 2);
		int prevMove 		= board.getMove(turn - 1);
		
		AbstractResponseList twoList = null;
			
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
		int key = levelTwoEncodedIndex(prevPrevMove, prevMove, colorToPlay);
		
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
