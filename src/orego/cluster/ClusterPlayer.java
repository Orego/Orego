package orego.cluster;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

/**
 * ClusterPlayer delegates MCTS to searchers on remote nodes via Java RMI
 */
public class ClusterPlayer extends Player implements SearchController {

	public static class RegistryFactory {
		public Registry getRegistry() throws RemoteException {
			return LocateRegistry.getRegistry();
		}
	}
	
	public static final String SECURITY_POLICY_MASTER = "/allow_all.policy";
	private static final String SEARCH_TIMEOUT_PROPERTY = "search_timeout";
	private static final String REMOTE_PLAYER_PROPERTY = "remote_player";
	private static final String MOVE_TIME_PROPERTY = "msec";

	// By default, use Lgrf2Player in the remote searchers
	private String remotePlayerClass = "Lgrf2Player"; 
	
	private List<TreeSearcher> remoteSearchers;
	
	private Map<String, String> remoteProperties;
	
	private int resultsRemaining;
	
	private long[] totalRuns;
	
	private long[] totalWins;
	
	// We wait for msecToMove if it is set, otherwise wait for timeout
	private long msecToMove = -1;
	
	// The default timeout for search is 10s
	private long msecToTimeout = 10000;
	
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

		// Configure RMI to allow serving the ClusterPlayer class
		RMIStartup.configureRmi(ClusterPlayer.class, SECURITY_POLICY_MASTER);
		
		// Publish ourself to the local registry
		Registry reg;
		try {
			reg = factory.getRegistry();
			reg.rebind(SearchController.SEARCH_CONTROLLER_NAME, this);
			
			// TODO: don't we need some cleanup code to unbind ourselves when we're finished?
		} catch (RemoteException e) {
			System.err.println("Fatal error. Could not publish ClusterPlayer to local registry.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Called by the TreeController to add a new remote searcher
	 */
	public void addSearcher(TreeSearcher s) {
		try {
			s.reset();
			s.setKomi(getBoard().getKomi());
			s.setPlayer(remotePlayerClass);
			for(String property : remoteProperties.keySet()) {
				s.setProperty(property, remoteProperties.get(property));
			}
			remoteSearchers.add(s);
		} catch (RemoteException e) {
			System.err.println("Error configuring new remote searcher: " + s);
		}
	}

	/**
	 * Called by searchers when they have results ready.
	 * Aggregates wins and runs information and keeps track of 
	 * how many searchers must still report.
	 */
	@Override
	public synchronized void acceptResults(TreeSearcher searcher,
			int[] runs, int[] wins) throws RemoteException {
		if (resultsRemaining < 0) {
			return;
		}
		resultsRemaining--;
		
		// aggregate all of the recommended moves from the player
		for (int idx = 0; idx < FIRST_POINT_BEYOND_BOARD; idx++) {
			totalRuns[idx] += runs[idx];
			totalWins[idx] += wins[idx];
		}
		if (resultsRemaining == 0) {
			searchLock.lock();
			searchDone.signal();
			searchLock.unlock();
		}
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
		
		// If we're here, we need to start searching, call up the searchers
		beginAcceptingResults();
		for (TreeSearcher searcher : remoteSearchers) {
			try {
				searcher.beginSearch();
			} catch (RemoteException e) {
				System.err.println("Searcher: " + searcher
						+ " failed to begin search.");
			}
		}
		
		// Wait for as long as we can
		long waitTime = msecToMove > 0 ? msecToMove : msecToTimeout;
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
		IntSet vacantPoints = getBoard().getVacantPoints();
		// TODO: Can we use the runs here too? Maybe a confidence interval?
		long maxWins = totalWins[PASS];
		int bestMove = PASS;
		for (int idx = 0; idx < vacantPoints.size(); idx++) {
			int point = vacantPoints.get(idx);
			long wins = totalWins[point];
			if (wins > maxWins) {
				maxWins = wins;
				bestMove = point;
			}
		}
		if (bestMove != PASS && getBoard().isFeasible(bestMove) && getBoard().isLegal(bestMove)) {
			return bestMove;
		}
		// TODO: We need better fallbacks, resign handling, etc.
		return super.bestMove();
	}

	/** Utility method to prepare to accept results */
	public synchronized void beginAcceptingResults() {
		resultsRemaining = remoteSearchers.size();
	}

	public synchronized void stopAcceptingResults() {
		resultsRemaining = -1;
	}
		
	/**
	 * Sets a property on the ClusterPlayer as well as all the currently
	 * connected Searchers. Properties are also stored to be set on searchers
	 * that connect in the future.
	 */
	@Override
	public void setProperty(String key, String value)
			throws UnknownPropertyException {
		if (key == REMOTE_PLAYER_PROPERTY) {
			// Do not forward the remote player property to the remote searchers
			remotePlayerClass = value;
			return;
		}
		if (key == SEARCH_TIMEOUT_PROPERTY) {
			msecToTimeout = Long.parseLong(value);
		}
		if (key == MOVE_TIME_PROPERTY) {
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
	
	@Override
	public synchronized int getWins(int p) {
		return (int) totalWins[p];
	}
	
	@Override
	public int getPlayouts(int p) {
		return (int) totalRuns[p];
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
