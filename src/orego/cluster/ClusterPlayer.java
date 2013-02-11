package orego.cluster;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
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

import orego.core.Board;
import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

/**
 * ClusterPlayer delegates MCTS to searchers on remote nodes via Java RMI
 */
public class ClusterPlayer extends Player {

	static class RegistryFactory {
		public Registry locateRegistry(String host) throws RemoteException {
			return LocateRegistry.getRegistry(host);
		}
	}
	
	private class ClusterSearchController extends UnicastRemoteObject implements
			SearchController {

		private static final long serialVersionUID = -7759373954975920875L;
		private ReentrantLock searchLock = null;
		private Condition searchDone = null;
		private int resultsRemaining;
		private int[] totalRuns;
		private int[] totalWins;

		protected ClusterSearchController() throws RemoteException {
			super();

			totalRuns = new int[FIRST_POINT_BEYOND_BOARD];
			totalWins = new int[FIRST_POINT_BEYOND_BOARD];
		}

		public synchronized int[] getTotalRuns() {
			return totalRuns;
		}

		public synchronized int[] getTotalWins() {
			return totalWins;
		}

		public synchronized void setSearchDone(Condition searchDone) {
			this.searchDone = searchDone;
		}

		public synchronized void setSearchLock(ReentrantLock searchLock) {
			this.searchLock = searchLock;
		}

		public synchronized void beginAcceptingResults(int nodeCount) {
			resultsRemaining = nodeCount;
		}

		public synchronized void stopAcceptingResults() {
			resultsRemaining = -1;
		}

		@Override
		public synchronized void acceptResults(TreeSearcher searcher,
				int[] runs, int[] wins) throws RemoteException {
			if (resultsRemaining < 0) {
				return;
			}
			resultsRemaining--;
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

	}

	private static final String SECURITY_POLICY_MASTER = "/allow_all.policy";
	private static final String SEARCH_HOSTS_PROPERTY = "search_hosts";
	private static final String SEARCH_TIMEOUT_PROPERTY = "search_timeout";
	private static final String MOVE_TIME_PROPERTY = "msec";

	private List<TreeSearcher> remoteSearchers;
	private List<String> searchHosts;
	private Map<String, String> remoteProperties;
	// We wait for msecToMove if it is set, otherwise wait for timeout
	private long msecToMove = -1;
	// The default timeout for search is 10s
	private long msecToTimeout = 10000;
	private ReentrantLock searchLock;
	private Condition searchDone;
	private ClusterSearchController controller;
	// The RegistryFactory used to get remote registries and for testing
	protected RegistryFactory factory = new RegistryFactory();

	/** Initializes RMI for the master */
	public ClusterPlayer() {
		super();
		remoteSearchers = new ArrayList<TreeSearcher>();
		searchHosts = new ArrayList<String>();
		remoteProperties = new HashMap<String, String>();

		// Set up lock and condition for search
		searchLock = new ReentrantLock();
		searchDone = searchLock.newCondition();
		try {
			controller = new ClusterSearchController();
		} catch (RemoteException e) {
			// If we can't create a controller, something is very wrong, fatal
			// error
			System.out
					.println("Error creating callback object ClusterSearchController:");
			e.printStackTrace();
			System.exit(1);
		}
		controller.setSearchLock(searchLock);
		controller.setSearchDone(searchDone);

		// Configure RMI with no published classes
		RMIStartup.configureRmi(null, SECURITY_POLICY_MASTER);
	}

	/**
	 * Resets the player, finds remoteSearchers, and resets them as well. Also
	 * sets resets the properties on all the searchers according the locally set
	 * properties.
	 */
	public void reset() {
		super.reset();
		remoteSearchers.clear();
		for (String address : searchHosts) {
			try {
				Registry reg = factory.locateRegistry(address);
				TreeSearcher searcher = (TreeSearcher) reg
						.lookup(TreeSearcher.SEARCHER_NAME);
				remoteSearchers.add(searcher);
				searcher.reset();
				searcher.setKomi(getBoard().getKomi());
				searcher.setController(controller);
			} catch (NotBoundException e) {
				System.err.println("Unable to find searcher at: " + address);
			} catch (RemoteException e) {
				System.err.println("Error connecting to repository at:" + address);
			}
		}
		for (String propertyKey : remoteProperties.keySet()) {
			try {
				setPropertyOnSearchers(propertyKey,
						remoteProperties.get(propertyKey));
			} catch (RemoteException e) {
				System.err.println("Error setting property " + propertyKey
						+ " on searchers.");
			}
		}
	}

	/** Forwards the played move to the remote searchers */
	public int acceptMove(int p) {
		int result = super.acceptMove(p);
		if (result == PLAY_OK) {
			TreeSearcher searcher = null;
			for (Iterator<TreeSearcher> it = remoteSearchers.iterator(); it
					.hasNext();) {
				searcher = it.next();
				try {
					searcher.acceptMove(opposite(getBoard().getColorToPlay()),
							p);
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
	public int bestMove() {
		// First check the opening book for a move
		if (getOpeningBook() != null) {
			int move = getOpeningBook().nextMove(getBoard());
			if (move != NO_POINT) {
				return move;
			}
		}
		// If we're here, we need to start searching, call up the searchers
		controller.beginAcceptingResults(remoteSearchers.size());
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
		controller.stopAcceptingResults();
		return bestSearchMove();
	}

	/** Decides on a move to play from the data gotten from searchers. */
	private int bestSearchMove() {
		IntSet vacantPoints = getBoard().getVacantPoints();
		// TODO: Can we use the runs here too? Maybe a confidence interval?
		int[] totalWins = controller.getTotalWins();
		int maxWins = totalWins[PASS];
		int bestMove = PASS;
		for (int idx = 0; idx < vacantPoints.size(); idx++) {
			int point = vacantPoints.get(idx);
			int wins = totalWins[point];
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

	/**
	 * Sets a property on the ClusterPlayer as well as all the currently
	 * connected Searchers. Properties are also stored to be set on searchers
	 * that connect in the future.
	 */
	public void setProperty(String key, String value)
			throws UnknownPropertyException {
		if (key == SEARCH_HOSTS_PROPERTY) {
			// "search_hosts" is expected to be a comma separated list of
			// hostnames or ip addresses
			String[] hosts = value.split(",");
			for (int idx = 0; idx < hosts.length; idx++) {
				hosts[idx] = hosts[idx].trim();
			}
			searchHosts = Arrays.asList(hosts);
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
		try {
			setPropertyOnSearchers(key, value);
		} catch (RemoteException e) {
			throw new UnknownPropertyException(
					"A remote searcher rejected the property: " + key);
		}
	}
	
	/** Sets the komi on the board and the remote searchers */
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
