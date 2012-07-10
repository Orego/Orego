package orego.response;

import static orego.core.Coordinates.PASS;

import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import orego.mcts.McPlayer;
import orego.mcts.McRunnable;
import orego.play.UnknownPropertyException;
import ec.util.MersenneTwisterFast;

/**
 * This player uses a form of last good reply.
 * @author sstewart
 *
 */

public class ResponsePlayer extends McPlayer {
	
	/** Threshold for the first level table*/
	private int one_threshold;
	
	/** Threshold for the second level table*/
	private int two_threshold;
	
	/** 
	 * Hashtable which stores best response lists.
	 * Each list is indexed by a bit masked 32 bit int with the following format:
	 * -----------------------
	 * | s |  ............ 0 | (all zeros) = zero response list
	 * -----------------------
	 * 
	 * -----------------------
	 * | s | ......... 1020101
	 * 
	 * The upper level bits are reserved for selecting between white and black and other meta data.
	 * The 28th bit selects between black and white.
	 */
	private HashMap<Integer, AbstractResponseList> responses;
	
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
		System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: " + benchMarkInfo[1]);
	}
	
	public ResponsePlayer(){
		super();
		responses = new HashMap<Integer, AbstractResponseList>();
		
		priorsWeight = DEFAULT_WEIGHT;
				
		// black level zero table
		responses.put(levelZeroEncodedIndex(Colors.BLACK), new RawResponseList());
		
		// white level zero table
		responses.put(levelZeroEncodedIndex(Colors.WHITE), new RawResponseList());

	}
	
	/** Converts a sequence of two moves into a hash key for our
	 * responses hashmap.
	 * @param prevPrevMove Two moves ago
	 * @param prevMove Previous move
	 * @return An integer key for our {@link responses} hashmap.
	 */
	public static int levelTwoEncodedIndex(int prevPrevMove, int prevMove, int color) {
		// place the color in the upper 28th bit
		return (1 << 27) | (prevPrevMove << 9) | prevMove;
	}
	
	public static int levelOneEncodedIndex(int prevMove, int color) {
		// place the prevMove in the lower bits
		// flip the color bit in the upper bits 
		return (color << 27) | prevMove;
	}
	
	public static int levelZeroEncodedIndex(int color) {
		// set the 29th bit to 1 to indicate zero table
		// set the 28th bit to indicate the color
		// TODO: we should use a signal bit (~3 in all three sections to indicate that we have a zero encoded index)
		return (1 << 28) | (color << 27);
	}
	
	/**
	 * Sets the weight for updateResponses (like updatePriors)
	 * @param weight the new weight
	 */
	public void setWeight(int weight) {
		priorsWeight = weight;
	}
	
	public void setOneThreshold(int threshold) {
		this.one_threshold = threshold;
	}
	
	public int getOneThreshold() {
		return one_threshold;
	}
	
	public void setTwoThreshold(int threshold) {
		this.two_threshold = threshold;
	}
	
	public int getTwoThreshold() {
		return two_threshold;
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
			move = findAppropriateMove(board, history1, history2, random);
			runnable.acceptMove(move);
			runnable.getPolicy().updateResponses(this, board, priorsWeight);
		}
	}

	// TODO: synchronize updateResponses to board?
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
			move = findAppropriateMove(board, history1, history2, random);
			runnable.acceptMove(move);
		}
	}
	
	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		Board board = runnable.getBoard();
		int toPlay = Colors.BLACK;
		for(int i = 0; i < board.getTurn(); i++) {
			updateWins(i, winner, board, toPlay);
			// flip to other player
			toPlay = 1 - toPlay;
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
		
		// we need a random generator
		// threads are stopped at this point so we are blithely unaware of synchronization concerns
		MersenneTwisterFast random = ((McRunnable)getRunnable(0)).getRandom();
		int move = findAppropriateMove(board, history1, history2, random);
		
		
		// TODO: we have an issue here with passing. We have to track stats on the pass move
		// to ensure it eventually looks "bad". However, since pass occurs during the end of every game,
		// it can also look "the best" for some move sequences. Currently this appears to be relatively 
		// insignificant as there are few moves which share a common two move prefix.
		AbstractResponseList res = responses.get(levelTwoEncodedIndex(history2, history1, board.getColorToPlay()));
		if(res != null && res.getWinRate(move) < 0.1) {
			return Coordinates.RESIGN;
		}
		return move;
	}

	/**
	 * Finds the right move to accept, given that the tables
	 * hold correct and updated information
	 * 
	 * @param level the level of table to explore
	 * @param board The current game board
	 * @param history1 the previous move
	 * @param history2 Two moves ago
	 * @param random A random number generator
	 * @return
	 */
	protected int findAppropriateMove(Board board, int history1, int history2, MersenneTwisterFast random) {
		int colorToPlay = board.getColorToPlay();
		
		// pick table based on threshold values
		AbstractResponseList list = responses.get(levelTwoEncodedIndex(history2, history1, colorToPlay));
		
		// can we use the level two table?
		if(list != null && list.getTotalRuns() >= two_threshold) {
			return list.bestMove(board, random);
		}
		
		list = responses.get(levelOneEncodedIndex(history1, colorToPlay));
		
		// can we use the level one table?
		if(list != null && list.getTotalRuns() >= one_threshold) {
			// can we use the level 1 table?
			return list.bestMove(board, random);
		} 
		
		list = responses.get(levelZeroEncodedIndex(colorToPlay));
		
		return list.bestMove(board, random);
	}
	
	
	
	/**
	 * Updates the win rates for the appropriate response lists. This method is called
	 * as we work our way down the "tree" or sequence of moves. If a win has been made through a given
	 * node (winner == toPlay) then we should add a win to that node. On turns 0 and 1 we have no choice
	 * but to use our zero and one tables, respectively.
	 * 
	 * @param turn which turn is being updated
	 * @param winner the winner of the playout
	 * @param board the board
	 * @param toPlay the color that played on this turn
	 */
	protected synchronized void updateWins(int turn, int winner, Board board, int colorToPlay) {
		
		AbstractResponseList twoList = null;
		
		// if we pass or we have made more than two moves, we can use the
		// level two list
		int prevPrevMove = board.getMove(turn - 2);
		int prevMove 	 = board.getMove(turn - 1);
		
		int key = levelTwoEncodedIndex(prevPrevMove, prevMove, colorToPlay);
		twoList = responses.get(key);
		
		if (twoList == null) { // if response list doesn't exist, create it
			twoList = new RawResponseList();
			responses.put(key, twoList);
		}
		
		if (winner == colorToPlay) // we've won through this node
			twoList.addWin(board.getMove(turn));
		else
			twoList.addLoss(board.getMove(turn)); // we've lost through this node
			
		
		/** other tables shouldn't track passes (see {@link bestStoredMove}) */
		if (board.getMove(turn) == Coordinates.PASS);
		
		AbstractResponseList zeroList = null;
		
		// use level zero table if we are making any move *but* a pass
		if (board.getMove(turn) != Coordinates.PASS) {
			// if first turn of game (0) or any other move,
			// we need to update the zero list
			key = levelZeroEncodedIndex(colorToPlay);
			
			zeroList = responses.get(key);
			
			if (zeroList == null) { // if the list doesn't exist, create it
				zeroList = new RawResponseList();
				responses.put(key, zeroList);
			}
			

			if (winner == colorToPlay) // we've won through this node
				zeroList.addWin(board.getMove(turn));
			else
				zeroList.addLoss(board.getMove(turn)); // we've lost through this node	
		}
		
		AbstractResponseList oneList = null;
	
		// update the level one table if we aren't passing
		if (board.getMove(turn) != Coordinates.PASS) {
			
			prevMove = board.getMove(turn - 1);
			key = levelOneEncodedIndex(prevMove, colorToPlay);
			
			oneList = responses.get(key);
			
			if (oneList == null) { // if list doesn't exist create it
				oneList = new RawResponseList();
				responses.put(key, oneList); 
			}
			
			if (winner == colorToPlay) // we've won through this node
				oneList.addWin(board.getMove(turn));
			else
				oneList.addLoss(board.getMove(turn)); // we've lost through this node
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
			one_threshold = Integer.parseInt(value);
		} else if (property.equals("two_threshold")) {
			two_threshold = Integer.parseInt(value);
		} else if (property.equals("priors")) {
			priorsWeight = Integer.parseInt(value);
		}
		else {
			super.setProperty(property, value);
		}
	}
	
	/**
	 * Add the specified number of wins to *all* tables for a given move
	 * 
	 * @param move The move we are biasing
	 * @param board The current state of the board
	 * @param wins The number of wins we'd like to bias
	 * @return
	 */
	public void addWins(int move, Board board, int wins) {
		// TODO: warning! - will these ever be called before we make at least two moves?
		
		int prevMove     = board.getMove(board.getTurn() - 1);
		int prevPrevMove = board.getMove(board.getTurn() - 2);
		int colorToPlay  = board.getColorToPlay();
		
		// get the zero level table (create if it doesn't exist)
		int key = levelZeroEncodedIndex(colorToPlay);
		
		AbstractResponseList zeroList = responses.get(key);
		
		if (zeroList == null) {
			zeroList = new RawResponseList();
			responses.put(key, zeroList);
		}
		
		// get the one level table (create if it doesn't exist)
		key = levelOneEncodedIndex(prevMove, colorToPlay);
		
		AbstractResponseList oneList = responses.get(key);
		
		if (oneList == null) {
			oneList = new RawResponseList();
			responses.put(key, oneList);
		}
		
		
		// get the second level table (create if it doesn't exist)
		key = levelTwoEncodedIndex(prevPrevMove, prevPrevMove, colorToPlay);
		
		AbstractResponseList twoList = responses.get(key);
		
		if (twoList == null) {
			twoList = new RawResponseList();
			responses.put(key, twoList);
		}

		for(int i = 0; i < wins; i++) {
			zeroList.addWin(move);
			oneList.addWin(move);
			twoList.addWin(move);
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
		StringBuilder builder = new StringBuilder(200);
		builder.append("Total Run Counts:");
		
		// TODO: do a frequency count of runs against moves in all level two entries for black
		// Note: we've removed this due to mashing everything together into the hashtable
		return builder.toString();
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
	
	public HashMap<Integer, AbstractResponseList> getResponses() {
		return responses;
	}

	
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
