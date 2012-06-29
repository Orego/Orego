package orego.response;

import static orego.core.Coordinates.PASS;
import ec.util.MersenneTwisterFast;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;

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
	// Response list holders -- zeroTables = {responseZeroBlack,responseZeroWhite} etc..
	private ResponseList[] zeroTables;
	private ResponseList[][] oneTables;
	private ResponseList[][][] twoTables;
	// testing flag
	private boolean testing = false;
	
	public ResponsePlayer(){
		super();
		int arrayLength = Coordinates.FIRST_POINT_BEYOND_BOARD;
		// create black tables
		responseZeroBlack = new ResponseList();
		responseOneBlack = new ResponseList[arrayLength];
		responseTwoBlack = new ResponseList[arrayLength][arrayLength];
		// create white tables
		responseZeroWhite = new ResponseList();
		responseOneWhite = new ResponseList[arrayLength];
		responseTwoWhite = new ResponseList[arrayLength][arrayLength];
		// initialize first and second levels
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
		// create holder tables
		zeroTables = new ResponseList[2];
		zeroTables[Colors.BLACK] = responseZeroBlack;
		zeroTables[Colors.WHITE] = responseZeroWhite;
		oneTables = new ResponseList[2][arrayLength];
		oneTables[Colors.BLACK] = responseOneBlack;
		oneTables[Colors.WHITE] = responseOneWhite;
		twoTables = new ResponseList[2][arrayLength][arrayLength];
		twoTables[Colors.BLACK] = responseTwoBlack;
		twoTables[Colors.WHITE] = responseTwoWhite;
	}
	
	/**
	 * toggle testing flag to change threshold value
	 * 
	 * @param setting new setting for testing flag
	 */
	public void setTesting(boolean setting) {
		testing = setting;
	}
	
	/**
	 * Force the given moves, then proceed as in generateMovesToFrontier
	 * Used for testing
	 * 
	 * @param runnable
	 * @param moves the moves to force the game to play
	 */
	public void fakeGenerateMovesToFrontierOfTree(McRunnable runnable, int... moves) {
		MersenneTwisterFast random = runnable.getRandom();
		Board board = runnable.getBoard();
		int move;
		int history1;
		int history2;
		// copy the board
		board.copyDataFrom(getBoard());
		// force play moves
		for (int i : moves) {
			runnable.acceptMove(i);
		}
		while (board.getPasses() < 2) {
			// play out like generateMovesToFrontier()
			history1 = board.getMove(board.getTurn() - 1);
			history2 = board.getMove(board.getTurn() - 2);
			move = findAppropriateLevelMove(board,history1,history2);
			runnable.acceptMove(move);
		}
	}

	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		MersenneTwisterFast random = runnable.getRandom();
		Board board = runnable.getBoard();
		int move;
		int history1;
		int history2;
		board.copyDataFrom(getBoard());
		while (board.getPasses() < 2) {
			// play out like generateMovesToFrontier()
			history1 = board.getMove(board.getTurn() - 1);
			history2 = board.getMove(board.getTurn() - 2);
			move = findAppropriateLevelMove(board, history1, history2);
			runnable.acceptMove(move);
		}
	}
	
	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		Board board = runnable.getBoard();
		int toPlay = Colors.BLACK;
		for(int i = 0; i <= board.getTurn(); i++) {
			updateWins(i, winner, board, toPlay);
			toPlay = 1-toPlay;
		}
	}
	
	@Override
	public int bestStoredMove() {
		Board board = getBoard();
		if (board.getPasses() == 1 && secondPassWouldWinGame()) {
			return PASS;
		}
		int history1 = board.getMove(board.getTurn() - 1);
		int history2 = board.getMove(board.getTurn() - 2);
		int move = findAppropriateLevelMove(board,history1,history2);
		return move;
	}
	
	/**
	 * Finds the right move to accept, given that the tables
	 * hold correct and updated information
	 * 
	 * @param level the level of table to explore
	 * @param board
	 * @param history1
	 * @param history2
	 * @return
	 */
	protected int findAppropriateLevelMove(Board board,int history1,int history2) {
		// should maybe put some asserts in here to make sure the indices aren't out of bounds
		// could make this a little fancier
		ResponseList table;
		int turn = board.getTurn();
		int toPlay = board.getColorToPlay();
		// pick table based on threshold values
		if(turn >= 2 && twoTables[toPlay][history2][history1].getTotalRuns() >= (testing ? TEST_THRESHOLD : THRESHOLD)) {
			table = twoTables[toPlay][history2][history1];
		} else if(turn >= 1 && oneTables[toPlay][history1].getTotalRuns() >= (testing ? TEST_THRESHOLD : THRESHOLD)) {
			table = oneTables[toPlay][history1];
		} else {
			table = zeroTables[toPlay];
		}
		// find the move
		int counter = 1;
		int move = table.getMoves()[0];
		while (move != Coordinates.PASS) {
			if (board.isLegal(move) && board.isFeasible(move)) {
				break;
			}
			move = table.getMoves()[counter];
			counter++;
		}
		return move;
	}
	
	/**
	 * Updates the appropriate response lists.
	 * 
	 * @param turn which turn is being updated
	 * @param winner the winner of the playout
	 * @param board the board
	 * @param toPlay the color that played on this turn
	 */
	protected synchronized void updateWins(int turn, int winner, Board board, int toPlay) {
		if(board.getMove(turn) == Coordinates.PASS) {
			// Always want Pass win rate to be 0.10
			return;
		}
		if (turn == 0) {
			// we assume black plays first (even in handicap games)
			if (winner == toPlay) {
				responseZeroBlack.addWin(board.getMove(0));
			} else {
				responseZeroBlack.addLoss(board.getMove(0));
			}
		} else if (turn == 1) {
			// similarly, assume white to play second
			if (winner == toPlay) {
				responseZeroWhite.addWin(board.getMove(1));
				responseOneWhite[board.getMove(0)].addWin(board.getMove(1));
			} else {
				responseZeroWhite.addLoss(board.getMove(1));
				responseOneWhite[board.getMove(0)].addLoss(board.getMove(1));
			}
		} else if(winner == toPlay) {
			// add wins
			zeroTables[toPlay].addWin(board.getMove(turn));
			oneTables[toPlay][board.getMove(turn - 1)].addWin(board.getMove(turn));
			twoTables[toPlay][board.getMove(turn - 2)][board.getMove(turn - 1)].addWin(board.getMove(turn));
		} else {
			// add losses
			zeroTables[toPlay].addLoss(board.getMove(turn));
			oneTables[toPlay][board.getMove(turn - 1)].addLoss(board.getMove(turn));
			twoTables[toPlay][board.getMove(turn - 2)][board.getMove(turn - 1)].addLoss(board.getMove(turn));
		}
	}
	
	public void reset() {
		super.reset();
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new McRunnable(this, getPolicy().clone()));
		}
	}
	
	// All of the getters
	public ResponseList getResponseZeroBlack() { return responseZeroBlack; }
	public ResponseList[] getResponseOneBlack() { return responseOneBlack; }
	public ResponseList[][] getResponseTwoBlack() { return responseTwoBlack; }
	public ResponseList getResponseZeroWhite() { return responseZeroWhite; }
	public ResponseList[] getResponseOneWhite() { return responseOneWhite; }
	public ResponseList[][] getResponseTwoWhite() { return responseTwoWhite; }
	
	// All of the inherited methods that we don't use
	/** Inherited, don't need it for ResponsePlayer */
	public void beforeStartingThreads() {}
	/** Inherited, don't need it for ResponsePlayer */
	public void updateForAcceptMove(int p) {}
	/** Inherited, don't need it for ResponsePlayer */
	public int getPlayouts(int p) { return 0;}
	/** Inherited, don't need it for ResponsePlayer */
	public double getWinRate(int p) {return 0;}
	/** Inherited, don't need it for ResponsePlayer */
	public int getWins(int p) {return 0;}
	/** Inherited, don't need it for ResponsePlayer */
	protected String winRateReport() {return "";}
}
