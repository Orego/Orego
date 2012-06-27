package orego.response;

import ec.util.MersenneTwisterFast;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.mcts.SearchNode;
import orego.mcts.TranspositionTable;
import orego.util.IntSet;
import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;

/**
 * TODO
 * 		Put white and black tables into an array
 * 	
 * 		Example:
 * 			ResponseList[] responsesZero = {responseZeroBlack, responseZeroWhite};
 * 			etc..
 *
 */

public class ResponsePlayer extends McPlayer {
	
	public static final int THRESHOLD = 100;
	public static final int TEST_THRESHOLD = 1;
	
	// Response lists for black
	private ResponseList responseZeroBlack;
	private ResponseList[] responseOneBlack;
	private ResponseList[][] responseTwoBlack;
	// Response lists for white
	private ResponseList responseZeroWhite;
	private ResponseList[] responseOneWhite;
	private ResponseList[][] responseTwoWhite;
	// testing flag
	private boolean testing = false;
	
	public ResponsePlayer(){
		super();
		int arrayLength = Coordinates.FIRST_POINT_BEYOND_BOARD;
		responseZeroBlack = new ResponseList();
		responseOneBlack = new ResponseList[arrayLength];
		responseTwoBlack = new ResponseList[arrayLength][arrayLength];
		responseZeroWhite = new ResponseList();
		responseOneWhite = new ResponseList[arrayLength];
		responseTwoWhite = new ResponseList[arrayLength][arrayLength];
		for (int i = 0; i < arrayLength; i++) {
			responseOneBlack[i] = new ResponseList();
			responseTwoBlack[i] = new ResponseList[arrayLength];
			responseOneWhite[i] = new ResponseList();
			responseTwoWhite[i] = new ResponseList[arrayLength];
			for (int j = 0; j < arrayLength; j++) {
				responseTwoBlack[i][j] = new ResponseList();
				responseTwoWhite[i][j] = new ResponseList();
			}
		}
	}
	
	/**
	 * toggle testing flag to change threshold value
	 * @param setting new setting for testing flag
	 */
	public void setTesting(boolean setting) {
		testing = setting;
	}
	
	/**
	 * 
	 * @param runnable
	 * @param moves the moves to force the game to play
	 */
	public void fakeGenerateMovesToFrontierOfTree(McRunnable runnable, int... moves) {
		MersenneTwisterFast random = runnable.getRandom();
		Board board = runnable.getBoard();
		board.copyDataFrom(getBoard());
		for(int move: moves) {
			runnable.acceptMove(move);
		}
		int move;
		ResponseList tableZero;
		ResponseList[] tableOne;
		ResponseList[][] tableTwo;
		// play out the game using tables
		while (board.getPasses() < 2) {
			int history1 = board.getMove(board.getTurn()-1);
			int history2 = board.getMove(board.getTurn()-2);
			tableZero = board.getColorToPlay() == Colors.BLACK ? responseZeroBlack : responseZeroWhite;
			tableOne = (ResponseList[]) (board.getColorToPlay() == Colors.BLACK ? responseOneBlack : responseOneWhite);
			tableTwo = (ResponseList[][]) (board.getColorToPlay() == Colors.BLACK ? responseTwoBlack : responseTwoWhite);
			// if we've met the threshold, check ResponseListTwo
			if (board.getTurn() >= 2 && tableTwo[history2][history1].getTotalRuns() 
					>= (testing ? TEST_THRESHOLD: THRESHOLD)) {
				int counter = 1;
				move = tableTwo[history2][history1].getMoves()[0];
				/*
				 * If pass is our best move, play it
				 * 
				 * Otherwise keep going down the table until
				 * we find a legal and feasible move
				 */
				while (move != Coordinates.PASS) {
					if(board.isLegal(move) && board.isFeasible(move)) {
						break;
					}
					move = tableTwo[history2][history1].getMoves()[counter];
					counter++;
				}
				runnable.acceptMove(move);
			}
			// same as above, but with ResponseListOne
			else if (board.getTurn() >= 1 && tableOne[history1].getTotalRuns() >= (testing ? TEST_THRESHOLD: THRESHOLD)) {
				int counter = 1;
				move = tableOne[history1].getMoves()[0];
				while (move != Coordinates.PASS) {
					if(board.isLegal(move) && board.isFeasible(move)) {
						break;
					}
					move = tableOne[history1].getMoves()[counter];
					counter++;
				}
				runnable.acceptMove(move);
			}
			// Same with ResponseListZero
			// will do even if there's no data
			else {
				int counter = 1;
				move = tableZero.getMoves()[0];
				while (move != Coordinates.PASS) {
					if(board.isLegal(move) && board.isFeasible(move)) {
						break;
					}
					move = tableZero.getMoves()[counter];
					counter++;
				}
				runnable.acceptMove(move);
			}
		}
	}

	public void generateMovesToFrontier(McRunnable runnable) {
		MersenneTwisterFast random = runnable.getRandom();
		Board board = runnable.getBoard();
		board.copyDataFrom(getBoard());
		int move;
		ResponseList tableZero;
		ResponseList[] tableOne;
		ResponseList[][] tableTwo;
		// play the first move in our list(s)
		while (board.getPasses() < 2) {
			int history1 = board.getMove(board.getTurn()-1);
			int history2 = board.getMove(board.getTurn()-2);
			tableZero = board.getColorToPlay() == Colors.BLACK ? responseZeroBlack : responseZeroWhite;
			tableOne = (ResponseList[]) (board.getColorToPlay() == Colors.BLACK ? responseOneBlack : responseOneWhite);
			tableTwo = (ResponseList[][]) (board.getColorToPlay() == Colors.BLACK ? responseTwoBlack : responseTwoWhite);
			if (board.getTurn() >= 2 && tableTwo[history2][history1].getTotalRuns() 
					>= (testing ? TEST_THRESHOLD: THRESHOLD)) {
				int counter = 1;
				move = tableTwo[history2][history1].getMoves()[0];
				// Play pass if it's the best move, otherwise find a legal and feasible
				// move in the list
				while (move != Coordinates.PASS) {
					if(board.isLegal(move) && board.isFeasible(move)) {
						break;
					}
					move = tableTwo[history2][history1].getMoves()[counter];
					counter++;
				}
				runnable.acceptMove(move);
			}
			else if (board.getTurn() >= 1 && tableOne[history1].getTotalRuns() >= (testing ? TEST_THRESHOLD: THRESHOLD)) {
				int counter = 1;
				move = tableOne[history1].getMoves()[0];
				// Play pass if it's the best move, otherwise find a legal and feasible
				// move in the list
				while (move != Coordinates.PASS) {
					if(board.isLegal(move) && board.isFeasible(move)) {
						break;
					}
					move = tableOne[history1].getMoves()[counter];
					counter++;
				}
				runnable.acceptMove(move);
			}
			else {
				int counter = 1;
				move = tableZero.getMoves()[0];
				// Play pass if it's the best move, otherwise find a legal and feasible
				// move in the list
				while (move != Coordinates.PASS) {
					if(board.isLegal(move) && board.isFeasible(move)) {
						break;
					}
					move = tableZero.getMoves()[counter];
					counter++;
				}
				runnable.acceptMove(move);
			}
		}
	}
	
	/**
	 * @param p the move
	 * 
	 * inherited from McPlayer -- nonsensical for response lists
	 */
	public int getPlayouts(int p) {
		return 0;
	}

	/**
	 * @param p the move
	 * 
	 * inherited from McPlayer -- nonsensical for response lists
	 */
	public double getWinRate(int p) {
		return 0;
	}

	/**
	 * inherited from McPlayer -- nonsensical for response lists
	 * 
	 * @param p the move
	 */
	public int getWins(int p) {
		return 0;
	}
	
	/**
	 * Updates the appropriate lists.
	 * 
	 * @param move the move number to update
	 * @param winner whoever won the playout
	 * @param board the board
	 */
	protected void updateWins(int winner, Board board) {
		// black plays all even moves, white all odd moves
		int toPlay = board.getTurn() % 2;
		int move = board.getTurn();
		if(move == 0) {
			// we assume black plays first (even in handicap games)
			if(winner==toPlay) {
				responseZeroBlack.addWin(board.getMove(0));
			}
			else {
				responseZeroBlack.addLoss(board.getMove(0));
			}
		}
		else if(move == 1) {
			// similarly, assume white to play
			if(winner==toPlay) {
				responseZeroWhite.addWin(board.getMove(1));
				responseOneWhite[board.getMove(0)].addWin(board.getMove(1));
			}
			else {
				responseZeroWhite.addLoss(board.getMove(1));
				responseOneWhite[board.getMove(0)].addLoss(board.getMove(1));
			}
		}
		else {
		// Updates the lists of player who made the current move
			if(Colors.BLACK == toPlay) { 
				if(Colors.BLACK == winner) {
					responseZeroBlack.addWin(board.getMove(move));
					responseOneBlack[board.getMove(move-1)].addWin(board.getMove(move));
					responseTwoBlack[board.getMove(move-2)][board.getMove(move-1)].addWin(board.getMove(move));
				}
				else {
					responseZeroBlack.addLoss(board.getMove(move));
					responseOneBlack[board.getMove(move-1)].addLoss(board.getMove(move));
					responseTwoBlack[board.getMove(move-2)][board.getMove(move-1)].addLoss(board.getMove(move));
				}
			}
			else { 
				if(Colors.WHITE == winner) {
					responseZeroWhite.addWin(board.getMove(move));
					responseOneWhite[board.getMove(move-1)].addWin(board.getMove(move));
					responseTwoWhite[board.getMove(move-2)][board.getMove(move-1)].addWin(board.getMove(move));
				}
				else {
					responseZeroWhite.addLoss(board.getMove(move));
					responseOneWhite[board.getMove(move-1)].addLoss(board.getMove(move));
					responseTwoWhite[board.getMove(move-2)][board.getMove(move-1)].addLoss(board.getMove(move));
				}
			}
		}
	}

	public void incorporateRun(int winner, McRunnable runnable) {
		Board board = runnable.getBoard();
		int toPlay = Colors.BLACK;
		// update for first two moves separately, since we can't use all tables
		updateWins(winner, board);
		toPlay = 1-toPlay;
		updateWins(winner, board);
		toPlay = 1-toPlay;
		// update the rest of the moves
		System.out.println(board.getTurn());
		for(int i = 2; i < board.getTurn(); i++) {
			updateWins(winner, board);
			toPlay = 1-toPlay;
		}
	}
	
	public void reset() {
		super.reset();
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new McRunnable(this, getPolicy().clone()));
		}
	}

	protected String winRateReport() {
		return "";
	}

	@Override
	public void beforeStartingThreads() {
		// TODO Auto-generated method stub

	}

	@Override
	public int bestStoredMove() {
		// consult the deepest table we have confidence in for a move
		if (getBoard().getTurn() >= 2 && responseTwoBlack[(getBoard().getTurn()-2)][getBoard().getTurn()-1].getTotalRuns() >= (testing ? TEST_THRESHOLD: THRESHOLD)) {
			return responseTwoBlack[(getBoard().getTurn()-2)][getBoard().getTurn()-1].getMoves()[0];
		}
		else if (getBoard().getTurn() >= 1 && responseOneBlack[getBoard().getTurn()-1].getTotalRuns() >= (testing ? TEST_THRESHOLD: THRESHOLD)) {
			return responseOneBlack[getBoard().getTurn()-1].getMoves()[0];
		}
		else {
			return responseZeroBlack.getMoves()[0];
		}
	}

	@Override
	public void updateForAcceptMove(int p) {
		// TODO Auto-generated method stub

	}

	public ResponseList getResponseZeroBlack() {
		return responseZeroBlack;
	}

	public ResponseList[] getResponseOneBlack() {
		return responseOneBlack;
	}

	public ResponseList[][] getResponseTwoBlack() {
		return responseTwoBlack;
	}
	
	public ResponseList getResponseZeroWhite() {
		return responseZeroWhite;
	}

	public ResponseList[] getResponseOneWhite() {
		return responseOneWhite;
	}

	public ResponseList[][] getResponseTwoWhite() {
		return responseTwoWhite;
	}
}
