package orego.response;

import static orego.core.Coordinates.PASS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import ec.util.MersenneTwisterFast;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.mcts.MctsPlayer;
import orego.play.UnknownPropertyException;
import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;

public class ResponsePlayer extends McPlayer {
	
	/**Threshold for the first level*/
	public static int ONE_THRESHOLD = 100;
	/**Threshold for the second level*/
	public static int TWO_THRESHOLD = 100;
	/**Threshold for testing*/
	public static int TEST_THRESHOLD = 1;
	
	/**Zero level for black*/
	private RawResponseList responseZeroBlack;
	/**First level for black*/
	private RawResponseList[] responseOneBlack;
	/**Second level for black*/
	private RawResponseList[][] responseTwoBlack;
	/**Zero level for white*/
	private RawResponseList responseZeroWhite;
	/**First level for white*/
	private RawResponseList[] responseOneWhite;
	/**Second level for white*/
	private RawResponseList[][] responseTwoWhite;
	/** Zero table holder, [0] = responseZeroBlack, [1] = responseZeroWhite */
	private RawResponseList[] zeroTables;
	/** One table holder, [0] = responseOneBlack, [1] = responseOneWhite */
	private RawResponseList[][] oneTables;
	/** Two table holder, [0] = responseTwoBlack, [1] = responseTwoWhite */
	private RawResponseList[][][] twoTables;
	/** Testing flag */
	private boolean testing = false;
	/** Weight for updateResponses */
	private int priorsWeight;
	/** Default weight for updateResponses */
	private static final int DEFAULT_WEIGHT = 1;
	
	public static void main(String[] args) {
		ResponsePlayer p = new ResponsePlayer();
		try {
			p.setProperty("policy", "Escape:Pattern:Capture");
			p.setProperty("threads", "2");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		double[] benchMarkInfo = p.benchmark();
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
				+ benchMarkInfo[1]);
	}
	
	public ResponsePlayer(){
		super();
		priorsWeight = DEFAULT_WEIGHT;
		int arrayLength = Coordinates.FIRST_POINT_BEYOND_BOARD;
		// create black tables
		responseZeroBlack = new RawResponseList();
		responseOneBlack = new RawResponseList[arrayLength];
		responseTwoBlack = new RawResponseList[arrayLength][arrayLength];
		// create white tables
		responseZeroWhite = new RawResponseList();
		responseOneWhite = new RawResponseList[arrayLength];
		responseTwoWhite = new RawResponseList[arrayLength][arrayLength];
		// initialize first and second levels
		for (int i = 0; i < arrayLength; i++) {
			responseOneBlack[i] = new RawResponseList();
			responseTwoBlack[i] = new RawResponseList[arrayLength];
			responseOneWhite[i] = new RawResponseList();
			responseTwoWhite[i] = new RawResponseList[arrayLength];
			for (int j = 0; j < arrayLength; j++) {
				responseTwoBlack[i][j] = new RawResponseList();
				responseTwoWhite[i][j] = new RawResponseList();
			}
		}
		// create holder tables
		zeroTables = new RawResponseList[2];
		zeroTables[Colors.BLACK] = responseZeroBlack;
		zeroTables[Colors.WHITE] = responseZeroWhite;
		oneTables = new RawResponseList[2][arrayLength];
		oneTables[Colors.BLACK] = responseOneBlack;
		oneTables[Colors.WHITE] = responseOneWhite;
		twoTables = new RawResponseList[2][arrayLength][arrayLength];
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
	 * Sets the weight for updateResponses (like updatePriors)
	 * @param weight the new weight
	 */
	public void setWeight(int weight) {
		priorsWeight = weight;
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
			runnable.getPolicy().updateResponses(this, board, priorsWeight);
		}
	}

	// synchronize updateResponses to board?
	@Override
	public void generateMovesToFrontier(McRunnable runnable) {
		MersenneTwisterFast random = runnable.getRandom();
		Board board = runnable.getBoard();
		int move;
		int history1;
		int history2;
		board.copyDataFrom(getBoard());
		runnable.getPolicy().updateResponses(this, board, priorsWeight);
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
		for(int i = 0; i < board.getTurn(); i++) {
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
		if(twoTables[board.getColorToPlay()][history2][history1].getWinRate(move) < 0.1) {
			return Coordinates.RESIGN;
		}
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
		RawResponseList table;
		int turn = board.getTurn();
		int toPlay = board.getColorToPlay();
		// pick table based on threshold values
		if(turn >= 3 && twoTables[toPlay][history2][history1].getTotalRuns() >= (testing ? TEST_THRESHOLD : TWO_THRESHOLD)) {
			table = twoTables[toPlay][history2][history1];
		} else if(turn >= 2 && oneTables[toPlay][history1].getTotalRuns() >= (testing ? TEST_THRESHOLD : ONE_THRESHOLD)) {
			table = oneTables[toPlay][history1];
		} else {
			table = zeroTables[toPlay];
		}
		// find the move
		int counter = 1;
		int move = table.bestMove(board);
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
			// Keep pass statistics only in the level two table
			if(winner == toPlay) {
				// add wins
				twoTables[toPlay][board.getMove(turn - 2)][board.getMove(turn - 1)].addWin(Coordinates.PASS);
			} else {
				// add losses
				twoTables[toPlay][board.getMove(turn - 2)][board.getMove(turn - 1)].addLoss(Coordinates.PASS);
			}
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

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("one_threshold")) {
			ONE_THRESHOLD = Integer.parseInt(value);
		} else if (property.equals("two_threshold")) {
			TWO_THRESHOLD = Integer.parseInt(value);
		} else if (property.equals("priors")) {
			priorsWeight = Integer.parseInt(value);
		}
		else {
			super.setProperty(property, value);
		}
	}
	
	/**
	 * add the specified number of wins to each table 
	 * @return
	 */
	public void addWins(int move, Board board, int wins) {
		int history1 = board.getMove(board.getTurn()-1);
		int history2 = board.getMove(board.getTurn()-2);
		RawResponseList tableZero = zeroTables[board.getColorToPlay()];
		RawResponseList[] tableOne = oneTables[board.getColorToPlay()];
		RawResponseList[][] tableTwo = twoTables[board.getColorToPlay()];
		for(int i = 0; i < wins; i++) {
			tableZero.addWin(move);
			tableOne[history1].addWin(move);
			tableTwo[history2][history1].addWin(move);
		}
	}
	
	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-total-runs");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("string/Total runs/gogui-total-runs");
		return result;
	}
	
	protected String goguiTotalRuns() {
		String result = "Total Run Counts:";
		TreeMap<Long,Long> data = new TreeMap<Long,Long>();
		for (RawResponseList[] tables: responseTwoBlack) {
			for (RawResponseList table: tables) {
				long runs = table.getTotalRuns();
				if (!data.containsKey(runs)) {
					data.put(runs, 1l);
					continue;
				}
				long entry = data.get(runs);
				entry++;
				data.put(runs, entry);
			}
		}
		for (long l: data.keySet()) {
			long count = data.get(l);
			result += "\n" + l + "\t" + count;
		}
		//orego.experiment.Debug.setDebugFile("/Network/Servers/maccsserver.lclark.edu/Users/rtakahashi/Documents/rundata.rtf");
		orego.experiment.Debug.debug(result);
		return result;
	}
	
	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("gogui-total-runs")) {
			result = goguiTotalRuns();
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}
	
	// All of the getters
	public RawResponseList[] getZeroTables() {	return zeroTables; }
	public RawResponseList[][] getOneTables() { return oneTables; }
	public RawResponseList[][][] getTwoTables() { return twoTables; }
	
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
