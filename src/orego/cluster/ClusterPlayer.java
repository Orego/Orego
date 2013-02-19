package orego.cluster;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.opposite;
import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.RESIGN;
import static orego.mcts.MctsPlayer.RESIGN_PARAMETER;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import ec.util.MersenneTwisterFast;

import orego.cluster.RMIStartup.RegistryFactory;
import orego.core.Board;
import orego.mcts.Lgrf2Player;
import orego.mcts.StatisticalPlayer;
import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

/**
 * ClusterPlayer delegates MCTS to searchers on remote nodes via Java RMI
 */
public class ClusterPlayer extends Player implements SearchController, StatisticalPlayer {
	
	private static final String SEARCH_TIMEOUT_PROPERTY = "search_timeout";
	private static final String REMOTE_PLAYER_PROPERTY = "remote_player";
	private static final String MOVE_TIME_PROPERTY = "msec";
	
	// By default, use Lgrf2Player in the remote searchers
	private String remotePlayerClass = Lgrf2Player.class.getName();
	
	private List<TreeSearcher> remoteSearchers;
	
	private Map<String, String> remoteProperties;
	
	private int resultsRemaining;
	
	private int nextSearcherId = 1;
	
	private long[] totalRuns;
	
	private long[] totalWins;
	
	private MersenneTwisterFast random;
	
	// We wait for msecToMove if it is set, otherwise wait for timeout
	private long msecToMove = -1;
	
	// The default timeout for search is 10s
	private long msecToTimeout = 10000;
	
	// By default, wait 100ms more than the time allotted to search
	// We stop waiting when all the players respond, so it doesn't matter
	// if this is longer than necessary
	private long latencyFudge = 100;
	
	private ReentrantLock searchLock;
	
	private Condition searchDone;
	
	// The RegistryFactory used to get remote registries and for testing
	protected static RegistryFactory factory = new RegistryFactory();

	
	public ClusterPlayer() {
		super();
		remoteSearchers = new ArrayList<TreeSearcher>();
		remoteProperties = new HashMap<String, String>();

		// Set up lock and condition for search
		searchLock = new ReentrantLock();
		searchDone = searchLock.newCondition();
		
		totalRuns = new long[FIRST_POINT_BEYOND_BOARD];
		totalWins = new long[FIRST_POINT_BEYOND_BOARD];
		
		// RNG used for fallback moves
		random = new MersenneTwisterFast();

		// Configure RMI to allow serving the ClusterPlayer class
		RMIStartup.configureRmi(ClusterPlayer.class, RMIStartup.SECURITY_POLICY_FILE);
		
		// Publish ourself to the local registry
		Registry reg;
		try {
			reg = factory.getRegistry();
			SearchController stub = (SearchController) UnicastRemoteObject.exportObject(this, 0);
			reg.rebind(SearchController.SEARCH_CONTROLLER_NAME, stub);
			
			// TODO: don't we need some cleanup code to unbind ourselves when we're finished?
		} catch (RemoteException e) {
			System.err.println("Fatal error. Could not publish ClusterPlayer to local registry.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Called by the TreeSearcher to add a new remote searcher
	 */
	public synchronized void addSearcher(TreeSearcher s) {
		try {
			s.setSearcherId(nextSearcherId);
			s.setKomi(getBoard().getKomi());
			s.setPlayer(remotePlayerClass);
			s.reset();
			for(String property : remoteProperties.keySet()) {
				s.setProperty(property, remoteProperties.get(property));
			}
			// If moves have already been played, sync up the new searcher
			int turn = getBoard().getTurn();
			int player = BLACK;
			if(turn > 0) {
				for(int moveIdx = 0; moveIdx < turn; moveIdx++) {
					s.acceptMove(player, getBoard().getMove(moveIdx));
					player = opposite(player);
				}
			}
			remoteSearchers.add(s);
			nextSearcherId++;
		} catch (RemoteException e) {
			System.err.println("Error configuring new remote searcher: " + s);
			e.printStackTrace();
		}
	}
	
	/** Called by TreeSearcher to remove itself from consideration */
	public synchronized void removeSearcher(TreeSearcher searcher) throws RemoteException {
		remoteSearchers.remove(searcher);
		
		decrementResultsRemaining();
	}

	/**
	 * Called by searchers when they have results ready.
	 * Aggregates wins and runs information and keeps track of 
	 * how many searchers must still report.
	 */
	@Override
	public synchronized void acceptResults(TreeSearcher searcher,
			long[] runs, long[] wins) throws RemoteException {
		if (resultsRemaining < 0) {
			return;
		}

		// aggregate all of the recommended moves from the player
		for (int idx = 0; idx < FIRST_POINT_BEYOND_BOARD; idx++) {
			totalRuns[idx] += runs[idx];
			totalWins[idx] += wins[idx];
		}
		decrementResultsRemaining();
	}
	
	/**
	 * Resets the player and all of the connected searchers.
	 * Searchers that fail to reset will be disconnected.
	 */
	@Override
	public void reset() {
		super.reset();
		
		TreeSearcher searcher = null;
		
		for (Iterator<TreeSearcher> it = remoteSearchers.iterator(); it.hasNext();) {
			searcher = it.next();
			
			try {
				searcher.reset();
			} catch (RemoteException e) {
				System.err.println("Searcher: " + searcher + " failed to reset.");
				it.remove();
			}
		}
	}

	/** 
	 * Forwards the played move to the remote searchers.
	 * Searchers that respond with an exception will be disconnected.
	 */
	@Override
	public int acceptMove(int p) {
		
		int result = super.acceptMove(p);
		
		if (result == PLAY_OK) {
			
			TreeSearcher searcher = null;
			
			for (Iterator<TreeSearcher> it = remoteSearchers.iterator(); it.hasNext();) {
				searcher = it.next();
				try {
					searcher.acceptMove(opposite(getBoard().getColorToPlay()), p);
				} catch (RemoteException e) {
					// If a searcher fails to accept a move, drop it from the
					// list
					System.err.println("Searcher: " + searcher
							+ " failed to accept move.");
					it.remove();
				}
			}
		}
		return result;
	}
	
	/**
	 * Forwards the request to undo to each searcher after undoing here.
	 */
	@Override
	public boolean undo() {
		
		// if we can't undo locally, it's a lost cause
		if(!super.undo()) {
			return false;
		}
		
		// try to undo on each searcher, removing those that fail
		TreeSearcher searcher = null;
		
		for (Iterator<TreeSearcher> it = remoteSearchers.iterator(); it.hasNext();) {
			searcher = it.next();
			boolean success = false;
			try {
				success = searcher.undo();
			} catch (RemoteException e) {
				System.err.println("Searcher: " + searcher
						+ " failed to undo.");
			}
			if(!success) {
				it.remove();
			}
		}

		return true;
	}

	/**
	 * Generates moves using the searchers. First, checks the local book to see
	 * if there is a move to play. Then, calls each searcher with the current
	 * search settings and waits for them to reply or time to expire.
	 */
	@Override
	public int bestMove() {
		// First check the opening book for a move
		if (getOpeningBook() != null) {
			
			int move = getOpeningBook().nextMove(getBoard());
			
			if (move != NO_POINT) {
				return move;
			}
		}
		
		// Check to see if we have searchers, if not, play a random move
		// Since searchers may come online at any time, a couple bad moves
		// is better than just resigning
		if(remoteSearchers.size() == 0) {
			return fallbackMove();
		}
		
		// If we're here, we need to start searching, call up the searchers
		beginAcceptingResults();
		for (TreeSearcher searcher : remoteSearchers) {
			try {
				searcher.beginSearch();
			} catch (RemoteException e) {
				System.err.println("Searcher: " + searcher
						+ " failed to begin search.");
				e.printStackTrace();
			}
		}
		
		// Wait for as long as we can
		long waitTime = msecToMove > 0 ? msecToMove + latencyFudge : msecToTimeout;
		try {
			searchLock.lock();
			searchDone.await(waitTime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.err.println("Search timed out or was interrupted.");
		} finally {
			searchLock.unlock();
		}
		// Determine the best move from the search results
		stopAcceptingResults();
		int move = bestSearchMove();
		Arrays.fill(totalRuns, 0);
		Arrays.fill(totalWins, 0);
		return move;
	}

	/** Decides on a move to play from the data received from searchers. */
	private int bestSearchMove() {
		IntSet vacantPoints = new IntSet(FIRST_POINT_BEYOND_BOARD);
		vacantPoints.copyDataFrom(getBoard().getVacantPoints());
		int bestMove = PASS;
		long maxWins = 0;
		while(vacantPoints.size() > 0) {
			maxWins = totalWins[PASS];
			bestMove = PASS;
			for (int idx = 0; idx < vacantPoints.size(); idx++) {
				int point = vacantPoints.get(idx);
				long wins = totalWins[point];
				if (wins > maxWins) {
					maxWins = wins;
					bestMove = point;
				}
			}
			// If the best move found isn't a pass, but it is illegal, try again but remove that move as a possibility
			if(bestMove != PASS && (!getBoard().isFeasible(bestMove) || !getBoard().isLegal(bestMove))) {
				vacantPoints.remove(bestMove);
			}
			else {
				// Otherwise, the best thing is either a pass or a legal move
				break;
			}
		}
		// Resign if the best win rate is less than a set percent
		double winRate = ((double) maxWins) / ((double) totalRuns[bestMove]);
		if(winRate < RESIGN_PARAMETER) {
			return RESIGN;
		}
		else {
			return bestMove;
		}
	}
	
	/** Use this instead of the super's fallback implementation, which calls undo */
	private int fallbackMove() {
		Board copyBoard = new Board();
		copyBoard.copyDataFrom(getBoard());
		int move = getHeuristics().selectAndPlayOneMove(random, copyBoard);
		if(getBoard().isLegal(move)) {
			return move;
		}
		return PASS;
	}

	/** Utility method to prepare to accept results */
	public synchronized void beginAcceptingResults() {
		resultsRemaining = remoteSearchers.size();
	}

	public synchronized void stopAcceptingResults() {
		if(resultsRemaining > 0) {
			System.err.println("Results missing from " + resultsRemaining  + " searchers.");
		}
		resultsRemaining = -1;
	}
	
	private synchronized void decrementResultsRemaining() {
		resultsRemaining--;
		if(resultsRemaining <= 0) {
			searchLock.lock();
			searchDone.signal();
			searchLock.unlock();
		}
	}
		
	/**
	 * Sets a property on the ClusterPlayer as well as all the currently
	 * connected Searchers. Properties are also stored to be set on searchers
	 * that connect in the future.
	 */
	@Override
	public void setProperty(String key, String value)
			throws UnknownPropertyException {
		if (key.equals(REMOTE_PLAYER_PROPERTY)) {
			// Do not forward the remote player property to the remote searchers
			remotePlayerClass = value;
			return;
		}
		if (key.equals(SEARCH_TIMEOUT_PROPERTY)) {
			msecToTimeout = Long.parseLong(value);
		}
		if (key.equals(MOVE_TIME_PROPERTY)) {
			msecToMove = Long.parseLong(value);
		}
		// Try to set the property on super, it's ok if it fails
		try {
			super.setProperty(key, value);
		} catch (UnknownPropertyException e) {
		}
		// Store the property for future use
		remoteProperties.put(key, value);
		// Try to set it on all currently connected searchers
		// very useful for forwarding GTP commands
		try {
			setPropertyOnSearchers(key, value);
		} catch (RemoteException e) {
			throw new UnknownPropertyException(
					"A remote searcher rejected the property: " + key);
		}
	}
	
	/** Sets the komi on the board and the remote searchers */
	@Override
	public void setKomi(double komi) {
		super.setKomi(komi);
		for(TreeSearcher searcher : remoteSearchers) {
			try {
				searcher.setKomi(komi);
			} catch (RemoteException e) {
				System.err.println("Could not set komi on remote searcher: " + searcher);
			}
		}
	}
	
	@Override
	public synchronized int getWins(int p) {
		return (int) totalWins[p];
	}
	
	@Override
	public synchronized int getPlayouts(int p) {
		return (int) totalRuns[p];
	}
	
	@Override
	public synchronized long[] getBoardWins() {
		return totalWins;
	}
	
	@Override
	public synchronized long[] getBoardPlayouts() {
		return totalRuns;
	}
	
	@Override
	public synchronized long getTotalPlayoutCount() {
		long totalPlayouts = 0;
		for(TreeSearcher searcher : remoteSearchers) {
			try {
				totalPlayouts += searcher.getTotalPlayoutCount();
			} catch (RemoteException e) {
				System.err.println("Could not get total playout count from searcher: " + searcher);
			}
		}
		return totalPlayouts;
	}
	
	@Override
	public int getMillisecondsPerMove() {
		return (int) Math.max(msecToMove, 0);
	}

	/** Utility method to set a property on all the currently known searchers. */
	protected void setPropertyOnSearchers(String key, String value)
			throws RemoteException {
		for (TreeSearcher searcher : remoteSearchers) {
			searcher.setProperty(key, value);
		}
	}
}
