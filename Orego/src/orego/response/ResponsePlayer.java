package orego.response;

import static orego.core.Coordinates.PASS;
import java.util.HashMap;
import java.util.Map;
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
	private int oneThreshold;
	
	/** Threshold for the second level table*/
	private int twoThreshold;
	
	// TODO Are we creating objects every time we need one of these?
	// If so, we should use plain ints (faster, but less safe).
	/**
	 * Simply debug information for tracking the last table
	 * "level" used for picking a move.
	 */
	public static enum TableLevel {
		LevelZero,
		LevelOne,
		LevelTwo
	};
	
	protected TableLevel lastTableLevel;
	
	/** 
	 * The large hash table of best response lists. 
	 * @see levelTwoEncodedIndex 
	 * @see levelOneEncodedIndex 
	 * @see levelZeroEncodedIndex
	 */
	private HashMap<Integer, AbstractResponseList> responses;
	
	private HashMap<Integer, HashMap<Long,Integer>> watchPoints;
	
	// TODO A better way might be for both orego.mcts.SearchNode and 
	// orego.response.AbstractResponsePlayer to implement some interface
	// with an update() method.
	/** Weight for updateResponses */
	protected int priorsWeight;
	
	/** Default weight for updateResponses */
	public static final int DEFAULT_WEIGHT = 1;
	
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
		responses = new HashMap<Integer, AbstractResponseList>();
		oneThreshold = 100;
		twoThreshold = 100;
		priorsWeight = DEFAULT_WEIGHT;

		// black level zero table
		responses.put(levelZeroEncodedIndex(Colors.BLACK), new RawResponseList());
		// white level zero table
		responses.put(levelZeroEncodedIndex(Colors.WHITE), new RawResponseList());
		
		watchPoints = new HashMap<Integer, HashMap<Long, Integer>>();
	}
	
	// TODO POSSIBLY convert these into a single method.
	/** Converts a sequence of two moves into a hash key for our
	 * responses hashmap. The {@see responses} maintains a mapping
	 * between a "history" of a given number of moves and a "response list"
	 * of possible candidate responses. The response list includes all possible moves
	 * along with information pertaining to the "choiceness" of the move.
	 * 
	 * We maintain three different /conceptual/ tables:
	 * Level Zero
	 * Level One
	 * Level Two
	 * 
	 * The level zero has a single response list which maintains stats for every possible move.
	 * Entries from this table are used mostly at the start of the game.
	 * 
	 * The level one table has a response list for every single. In other words, for any given previous move,
	 * the table returns a list of possible new moves with given stats (response list).
	 * 
	 * The level two table has a response list for every combination of previous *two* moves. Hence, for the
	 * move sequence AB the level two table contains a response list of possible response.
	 * 
	 * We encode each of these types of tables as integers for keys in our responses hashmap. Level zero keys are encoded
	 * as integers with each group of nine bits set to a special sentinel value. 
	 * 
	 * Level one keys encode the previous move in the first nine lower order index.
	 * 
	 * Level two keys encode the previous two moves in the first nine lower order bits and then the second
	 * nine bits. Conceivably we can store up to three different "levels" of moves which would consume 24 bits.
	 * We need nine bits for each move since we have a maximum move index of 361.
	 * 
	 * We reserve the 28th bit to indicate the color of the player.
	 * 
	 * @param prevPrevMove Two moves ago
	 * @param prevMove Previous move
	 * @return An integer key for our {@link responses} hashmap.
	 */
	public static int levelTwoEncodedIndex(int prevPrevMove, int prevMove, int color) {
		// place the color in the upper 28th bit
		return (color << 27) | (prevPrevMove << 9) | prevMove;
	}
	
	public static int levelOneEncodedIndex(int prevMove, int color) {
		// place the prevMove in the lower bits
		// flip the color bit in the upper bits 
		return (color << 27) | prevMove;
	}
	
	public static int levelZeroEncodedIndex(int color) {
		// set the 28th bit to indicate the color
		// set all the lower order "groups" of 9 to the ZERO_LEVEL_SENTINEL
		return (color << 27) | (Coordinates.ZERO_LEVEL_SENTINEL << 18) | 
							   (Coordinates.ZERO_LEVEL_SENTINEL << 9)  | 
							   (Coordinates.ZERO_LEVEL_SENTINEL);
	}
	
	/**
	 * Sets the weight for updateResponses (like updatePriors)
	 * @param weight the new weight
	 */
	public void setWeight(int weight) {
		priorsWeight = weight;
	}
	
	public void setOneThreshold(int threshold) {
		this.oneThreshold = threshold;
	}
	
	public int getOneThreshold() {
		return oneThreshold;
	}
	
	public void setTwoThreshold(int threshold) {
		this.twoThreshold = threshold;
	}
	
	public int getTwoThreshold() {
		return twoThreshold;
	}
	
	/**
	 * Play the given moves. Don't play to the end, that is handled by fakeRun
	 * 
	 * @param runnable
	 * @param moves the moves to force the game to play
	 */
	public void fakePlayMoves(McRunnable runnable, int... moves) {
		Board board = runnable.getBoard();
		// copy the board
		board.copyDataFrom(getBoard());
		// force play moves
		for (int i : moves) {
			runnable.acceptMove(i);
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
		// TODO We'll be doing this differently in the future
		runnable.getPolicy().updateResponses(this, board, priorsWeight);
		while (board.getPasses() < 2) {
			history1 = board.getMove(board.getTurn() - 1);
			history2 = board.getMove(board.getTurn() - 2);
			move = findAppropriateMove(board, history1, history2, random, false);
			runnable.acceptMove(move);
		}
	}
	
	private void instrumentWatchPoints(Board board, int prevPrevMove, int prevMove, int move, int colorToPlay) {
		if (colorToPlay == Colors.WHITE) {

			HashMap<Long, Integer> watched = watchPoints.get(levelTwoEncodedIndex(prevPrevMove, prevMove, Colors.WHITE));
			
			if (watched == null) {
				watched = new HashMap<Long, Integer>();
				watchPoints.put(levelTwoEncodedIndex(prevPrevMove, prevMove, Colors.WHITE), watched);
			}
			
			int frequency = 1;
			if (watched.containsKey(board.getHash())) {
				frequency = watched.get(board.getHash());
				frequency++;
			}
		
			watched.put(board.getHash(), frequency);
		}
		// actually play the move
		board.play(move);
	}
	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		int toPlay = getBoard().getColorToPlay(); // current color
		
		Board diagnosticBoard = new Board();
		diagnosticBoard.copyDataFrom(getBoard());
		
		// move through all moves after we do a playout 
		// we start at our current state and move to the state of the runnable
		for(int i = getBoard().getTurn(); i < runnable.getBoard().getTurn(); i++) {
			updateWins(i, winner, runnable.getBoard(), toPlay);
			
			instrumentWatchPoints(diagnosticBoard, 
								  runnable.getBoard().getMove(i - 2), 
								  runnable.getBoard().getMove(i - 1),
								  runnable.getBoard().getMove(i),
								  toPlay);
			
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
		// threads are stopped at this point so we needn't worry about synchronization
		MersenneTwisterFast random = ((McRunnable)getRunnable(0)).getRandom();
		int move = findAppropriateMove(board, history1, history2, random, true);
		// TODO: we have an issue here with passing. We have to track stats on the pass move
		// to ensure it eventually looks "bad". However, since pass occurs during the end of every game,
		// it can also look "the best" for some move sequences. Currently this appears to be relatively 
		// insignificant as there are few moves which share a common two move prefix.
		AbstractResponseList res = responses.get(levelTwoEncodedIndex(history2, history1, board.getColorToPlay()));
		// TODO What if res.getWinRate(PASS) < 0.1? (1/10 might not be equal to 0.1)
		if(res != null && res.getWinRate(move) < 0.1) {
			return Coordinates.RESIGN;
		}
		return move;
	}
	
	// TODO Clarify the point of isFinalMove
	/**
	 * Finds the right move to accept, given that the tables
	 * hold correct and updated information
	 * 
	 * @param board The current game board
	 * @param history1 the previous move
	 * @param history2 Two moves ago
	 * @param random A random number generator
	 * @param isFinalMove Indicates we are picking the final move (used for testing)
	 * @return
	 */
	protected int findAppropriateMove(Board board, int history1, int history2, MersenneTwisterFast random, boolean isFinalMove) {
		int colorToPlay = board.getColorToPlay();
		// can we use the level two table?
		AbstractResponseList list = responses.get(levelTwoEncodedIndex(history2, history1, colorToPlay));
		if(list != null && list.getTotalRuns() >= twoThreshold) {
			// debugging
			if (isFinalMove) {
				lastTableLevel = TableLevel.LevelTwo;	
			}
			return list.bestMove(board, random);
		}
		// can we use the level one table?
		list = responses.get(levelOneEncodedIndex(history1, colorToPlay));		
		if(list != null && list.getTotalRuns() >= oneThreshold) {
			// debugging
			if (isFinalMove) {
				lastTableLevel = TableLevel.LevelOne;
			}
			return list.bestMove(board, random);
		} 
		// Use level zero table
		list = responses.get(levelZeroEncodedIndex(colorToPlay));
		// debugging
		if (isFinalMove) {
			lastTableLevel = TableLevel.LevelZero;
		}
		return list.bestMove(board, random);
	}
	
	/**
	 * Updates the win rates for the appropriate response lists. This method is called
	 * as we work our way down the "tree" or sequence of moves. If a win has been made through a given
	 * node (winner == toPlay) then we should add a win to that node.
	 * 
	 * @param turn which turn is being updated
	 * @param winner the winner of the playout
	 * @param board the board
	 * @param toPlay the color that played on this turn
	 */
	protected synchronized void updateWins(int turn, int winner, Board board, int colorToPlay) {
		// update the level one table if we aren't passing
		AbstractResponseList oneList;
		int prevMove = board.getMove(turn - 1);
		int oneKey = levelOneEncodedIndex(prevMove, colorToPlay);
		oneList = responses.get(oneKey);
		AbstractResponseList twoList;
		// We only track passes and most other moves in the two table.
		// We don't add the entry to the two table unless we've
		// seen it once and added it to the one level table
		// This saves space.
		int prevPrevMove 	= board.getMove(turn - 2);
		int twoKey = levelTwoEncodedIndex(prevPrevMove, prevMove, colorToPlay);
		twoList = responses.get(twoKey);
		// only add to the two list if we have seen the move in the one list
		// this filters out the infrequent nodes and saves space
		if (oneList != null) {
			if (twoList == null) { // if response list doesn't exist, create it
				twoList = new RawResponseList();
				responses.put(twoKey, twoList);
			}
			if (winner == colorToPlay) // we've won through this node
				twoList.addWin(board.getMove(turn));
			else
				twoList.addLoss(board.getMove(turn)); // we've lost through this node
			
		}
		// we should only track PASS wins/losses in the the two level table.
		// other tables shouldn't track passes
		if (board.getMove(turn) == Coordinates.PASS) {
			return;
		}
		if (oneList == null) { // if list doesn't exist create it
			oneList = new RawResponseList();
			responses.put(oneKey, oneList); 
		}
		// finally add the entry to the one list
		if (winner == colorToPlay) { // we've won through this node
			oneList.addWin(board.getMove(turn));
		} else {
			oneList.addLoss(board.getMove(turn)); // we've lost through this node
		}
		AbstractResponseList zeroList;
		// use level zero table if we are making any move *but* a pass
		int zeroKey = levelZeroEncodedIndex(colorToPlay);
		zeroList = responses.get(zeroKey);
		if (zeroList == null) { // if the list doesn't exist, create it
			zeroList = new RawResponseList();
			responses.put(zeroKey, zeroList);
		}
		if (winner == colorToPlay) // we've won through this node
			zeroList.addWin(board.getMove(turn));
		else
			zeroList.addLoss(board.getMove(turn)); // we've lost through this node	
	}
	
	public void reset() {
		super.reset();
		responses = new HashMap<Integer, AbstractResponseList>();
		// black level zero table
		responses.put(levelZeroEncodedIndex(Colors.BLACK), new RawResponseList());
		// white level zero table
		responses.put(levelZeroEncodedIndex(Colors.WHITE), new RawResponseList());
		for (int i = 0; i < getNumberOfThreads(); i++) {
			setRunnable(i, new McRunnable(this, getPolicy().clone()));
		}
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("one_threshold")) {
			oneThreshold = Integer.parseInt(value);
		} else if (property.equals("two_threshold")) {
			twoThreshold = Integer.parseInt(value);
		} else if (property.equals("priors")) {
			priorsWeight = Integer.parseInt(value);
		}
		else {
			super.setProperty(property, value);
		}
	}
	
	// TODO Maybe it would be easier to just create all three of them
	/**
	 * Add the specified number of wins to *all* tables for a given move
	 * 
	 * @param move The move we are biasing
	 * @param board The current state of the board
	 * @param wins The number of wins we'd like to bias
	 * @return
	 */
	public void addWins(int move, Board board, int wins) {		
		int prevMove     = board.getMove(board.getTurn() - 1);
		int prevPrevMove = board.getMove(board.getTurn() - 2);
		int colorToPlay  = board.getColorToPlay();
		// get the zero level table (create if it doesn't exist)
		int key = levelZeroEncodedIndex(colorToPlay);
		AbstractResponseList zeroList = responses.get(key);		
		// only add the move if it isn't a pass (we only track pass in two table)
		if (move != Coordinates.PASS && 
			zeroList == null) {
			zeroList = new RawResponseList();
			responses.put(key, zeroList);
		}
		// get the one level table (create if it doesn't exist)
		key = levelOneEncodedIndex(prevMove, colorToPlay);
		AbstractResponseList oneList = responses.get(key);
		// get the second level table (create if it doesn't exist)
		key = levelTwoEncodedIndex(prevPrevMove, prevPrevMove, colorToPlay);
		AbstractResponseList twoList = responses.get(key);
		// we should only create a two table entry if we have seen the move
		// before (i.e. not in the one table).
		// This allows us to save space for moves which we've seen only once
		if (oneList != null && twoList == null) {
			twoList = new RawResponseList();
			responses.put(key, twoList);
		}
		// finally create the one list (if not a pass move)
		if (move != Coordinates.PASS && 
			oneList == null) {
			oneList = new RawResponseList();
			responses.put(key, oneList);
		}
		for(int i = 0; i < wins; i++) {
			if (oneList != null) zeroList.addWin(move);
			if (oneList != null) oneList.addWin(move);
			if (twoList != null) twoList.addWin(move);
		}
	}
	
	/**
	 * Add the specified number of losses to *all* tables for a given move
	 * 
	 * @param move The move we are biasing
	 * @param board The current state of the board
	 * @param wins The number of losses we'd like to bias
	 */
	public void addLosses(int move, Board board, int losses) {	
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
		
		// get the second level table (create if it doesn't exist)
		key = levelTwoEncodedIndex(prevPrevMove, prevPrevMove, colorToPlay);
		
		AbstractResponseList twoList = responses.get(key);
		
		// we should only create a two table entry if we have seen the move
		// before (i.e. not in the one table).
		// This allows us to save space for moves which we've seen only once

		if (oneList != null && twoList == null) {
			twoList = new RawResponseList();
			responses.put(key, twoList);
		}
			
		// finally create the one list
		if (oneList == null) {
			oneList = new RawResponseList();
			responses.put(key, oneList);
		}

		for(int i = 0; i < losses; i++) {
			if (move != Coordinates.PASS) zeroList.addLoss(move);
			if (move != Coordinates.PASS) oneList.addLoss(move);
			if (twoList != null) twoList.addLoss(move);
		}
	}
	
	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-total-runs");
		result.add("gogui-last-table");
		result.add("gogui-count-points");
		result.add("gogui-clear-points");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		// format: type/label/command
		result.add("string/Total runs/gogui-total-runs");
		result.add("string/Last Table/gogui-last-table");
		result.add("string/Count Points/gogui-count-points %P");
		result.add("string/Clear Points/gogui-clear-points");
		return result;
	}
	
	protected String goguiTotalRuns() {
		StringBuilder builder = new StringBuilder(200);
		builder.append("Total Run Counts:");
		// Note: do a frequency count of runs against moves in all level two entries for black
		// Note: we've removed this due to mashing everything together into the hashtable
		return builder.toString();
	}
	
	protected String goguiLastTable() {
		return "Last table: " + lastTableLevel;
	}
	
	protected String goguiCountPoints(StringTokenizer arguments) {
		StringBuilder builder = new StringBuilder(); 
		
		int prevPrevMove = Coordinates.at(arguments.nextToken());
		int prevMove = Coordinates.at(arguments.nextToken());
		
		HashMap<Long, Integer> watched = watchPoints.get(levelTwoEncodedIndex(prevPrevMove, prevMove, Colors.WHITE));
		
		if (watched == null) {
			return "No Entries for (" + prevPrevMove + " " + prevMove + ")";
		}
		
		Set<Map.Entry<Long, Integer>> entries = watched.entrySet();
		int counter = 0; // x values
		for (Map.Entry<Long, Integer> entry : entries) {
			builder.append(counter + ", " + entry.getValue() + "\n");
			counter++;
		}
		
		return builder.toString();
	}
	
	protected String goguiClearPoints() {
		watchPoints.clear();

		
		return "Count: " + watchPoints.size();
	}
	
	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("gogui-total-runs")) {
			result = goguiTotalRuns();
		} else if (command.equals("gogui-last-table")) {
			result = goguiLastTable();
		} else if (command.equals("gogui-count-points")) {
			result = goguiCountPoints(arguments);
		} else if (command.equals("gogui-clear-points")) {
			result = goguiClearPoints();
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
	public int getPlayouts(int p) { return -1;}
	
	/** Inherited, don't need it for ResponsePlayer */
	public double getWinRate(int p) {return -1;}
	
	/** Inherited, don't need it for ResponsePlayer */
	public int getWins(int p) {return -1;}
	
	/** Inherited, don't need it for ResponsePlayer */
	protected String winRateReport() {return "";}

}
