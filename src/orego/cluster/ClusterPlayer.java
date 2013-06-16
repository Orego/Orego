package orego.cluster;

import static java.lang.Math.max;
import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;
import static orego.core.Coordinates.RESIGN;
import static orego.core.Coordinates.pointToString;
import static orego.mcts.MctsPlayer.RESIGN_PARAMETER;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import orego.cluster.RMIStartup.RegistryFactory;
import orego.core.Board;
import orego.core.Coordinates;
import orego.mcts.Lgrf2Player;
import orego.mcts.StatisticalPlayer;
import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

/**
 * ClusterPlayer delegates MCTS to searchers on remote nodes via Java RMI
 */
public class ClusterPlayer extends Player implements SearchController, StatisticalPlayer {
	
	private static final String SEARCH_TIMEOUT_PROPERTY = "search_timeout";
	private static final String REMOTE_PLAYER_PROPERTY = "remote_player";
	private static final String MOVE_TIME_PROPERTY = "msec";
	private static final String CLUSTER_PLAYER_INDEX = "cluster_player_index";
	private static final String LOG_FILE_PROPERTY = "cluster_player_log";
	
	// By default, use Lgrf2Player in the remote searchers
	private String remotePlayerClass = Lgrf2Player.class.getName();
	
	private List<TreeSearcher> remoteSearchers;
	
	private Map<String, String> remoteProperties;
	
	private PrintWriter logWriter;
	
	protected int resultsRemaining;
	
	/** The total number of times we attempt to force a client to accept a move before giving up*/
	private int MAX_ACCEPT_MOVE_ATTEMPTS = 5;
	
	/** the player index of this machine. This must be a non-negative value
	 * for us to bind properly and can only be set via .setProperty().
	 * @see #setProperty(String, String).
	 */
	protected int playerIndex = -1;
	
	private int nextSearcherId = 0;
	
	protected long[] totalRuns;
	
	protected long[] totalWins;
	
	private MersenneTwisterFast random;
	
	// We wait for msecToMove if it is set, otherwise wait for timeout
	private long msecToMove = -1;
	
	// The default timeout for search is 10s
	private long msecToTimeout = 10000;
	
	// By default, wait 100ms more than the time allotted to search
	// We stop waiting when all the players respond, so it doesn't matter
	// if this is longer than necessary
	private long latencyFudge = 1000;
	
	private ReentrantLock searchLock;
	
	private Condition searchDone;
	
	private Object searcherLockObject = new Object();
	
	// The RegistryFactory used to get remote registries and for testing
	protected static RegistryFactory factory = new RegistryFactory();

	/** Simple closure that represents an action that should be performed multiple times on the searcher*/
	private abstract static class ResiliantSearcherAction{
		public abstract void run() throws RemoteException;
		public abstract void failed();
	}
	
	public ClusterPlayer() {
		super();
		// we need a copy on write array list to avoid concurrent modification exceptions.
		// the number of searchers is small enough that this is fine.
		remoteSearchers = new CopyOnWriteArrayList<TreeSearcher>();
		remoteProperties = new HashMap<String, String>();
		
		// Set up the PrintWriter that will be used for error messages
		setLogWriter(new PrintWriter(System.err, true)); 
		
		// Set up lock and condition for search
		searchLock = new ReentrantLock();
		searchDone = searchLock.newCondition();
		
		totalRuns = new long[FIRST_POINT_BEYOND_BOARD];
		totalWins = new long[FIRST_POINT_BEYOND_BOARD];
		
		// RNG used for fallback moves
		random = new MersenneTwisterFast();

		// Reset to avoid null pointer issues when cluster searchers connect
		reset();
		
		// Note: we only actually bind to the RMI registry once someone calls .setProperty('cluster_player_index', [index value])
		// along with the index since we don't want to unbind and then rebind every time.
	}
	
	/**
	 * Un-binds an existing version of ourselves from the rmi registry.
	 */
	protected void unbindRMI() {
		if (this.playerIndex < 0) return; // never bound
		
		// check to see if we are in the local registry
		Registry reg;
		try {
			UnicastRemoteObject.unexportObject(this, true);
			
			reg = factory.getRegistry();
			
			String name = SearchController.SEARCH_CONTROLLER_NAME + playerIndex;
			
			for (String boundName : reg.list()) {
				if (boundName.equals(name)) {
					reg.unbind(boundName);
					break;
				}
			}
			
		
		} catch (RemoteException e) {
			System.err.println("Fatal error. Could not publish " + getName() + " to local registry with ID: " + playerIndex);
			e.printStackTrace();
			System.exit(1);
		} catch (NotBoundException e) {
			getLogWriter().println("Error: Could not unbind " + getName() + " because it was not bound. Continuing.");
			e.printStackTrace(getLogWriter());
		}
	}
	
	/**
	 * Method to actually bind this object in the RMI registry
	 * @param int playerIndex an optional player index parameter to allow multiple players. If it is < 0 we don't use it.
	 */
	protected void bindRMI() {
		if (this.playerIndex < 0) return; // if we don't have a valid player index, just return
		// Configure RMI to allow serving the ClusterPlayer class
		RMIStartup.configureRmi(ClusterPlayer.class, RMIStartup.SECURITY_POLICY_FILE);
		
		// Publish ourself to the local registry
		Registry reg;
		try {
			reg = factory.getRegistry();
			SearchController stub = (SearchController) UnicastRemoteObject.exportObject(this, 0);
			
			String name = SearchController.SEARCH_CONTROLLER_NAME + playerIndex;
			
			reg.rebind(name, stub);
			
		} catch (RemoteException e) {
			System.err.println("Fatal error. Could not publish ClusterPlayer to local registry.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Called by the TreeSearcher to add a new remote searcher
	 * @throws RemoteException 
	 */
	public void addSearcher(TreeSearcher s) throws RemoteException {
		synchronized (searcherLockObject) {
			try {
				getLogWriter().println("Adding searcher. ID will be " + nextSearcherId);
				s.setSearcherId(nextSearcherId);
				s.setKomi(getBoard().getKomi());
				s.setPlayer(remotePlayerClass);
				for(String property : remoteProperties.keySet()) {
					s.setProperty(property, remoteProperties.get(property));
				}
				s.reset();
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
				getLogWriter().println("Done adding searcher with ID " + nextSearcherId);
			} catch (RemoteException e) {
				System.err.println("Error configuring new remote searcher: " + s);
				e.printStackTrace();
			}
		}
	}
	
	public List<TreeSearcher> getRemoteSearchers() {
		return remoteSearchers;
	}
	
	/** Called by TreeSearcher to remove itself from consideration */
	public void removeSearcher(TreeSearcher searcher) throws RemoteException {
		unregisterSearcher(searcher);
	}
	
	private void unregisterSearcher(TreeSearcher searcher) {
		getLogWriter().println("Removing remote tree searcher because someone complained or it asked for it: " + searcher);
		
		remoteSearchers.remove(searcher);
		
		if (remoteSearchers.isEmpty())
			getLogWriter().println("Error: we have no more tree searchers. Birth was the death of me.");
		
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
		getLogWriter().println("Accepting results. " + resultsRemaining + " remaining.");
		
		if (resultsRemaining < 0) {
			return;
		}
		
		tallyResults(searcher, runs, wins);
		
		decrementResultsRemaining();
	}
	
	public void tallyResults(TreeSearcher searcher, long[] runs, long[] wins) {
		// aggregate all of the recommended moves from the player
		for (int idx = 0; idx < FIRST_POINT_BEYOND_BOARD; idx++) {
			totalRuns[idx] += runs[idx];
			totalWins[idx] += wins[idx];
		}
	}
	
	/**
	 * Resets the player and all of the connected searchers.
	 * Searchers that fail to reset will be disconnected.
	 */
	@Override
	public void reset() {
		super.reset();
		
		
		
		for (Iterator<TreeSearcher> it = remoteSearchers.iterator(); it.hasNext();) {
			final TreeSearcher searcher = it.next();
			
			tellSearcherToDoSomething(new ResiliantSearcherAction() {

				@Override
				public void run() throws RemoteException {
					searcher.reset();

				}

				@Override
				public void failed() {
					unregisterSearcher(searcher);

				}
			});
		}
	}
	
	/**
	 * Tells the searchers that we are going to die.
	 * This cluster player does a "fire and forget" and does not actually care if the searchers
	 * respond to the kill message.
	 * 
	 */
	@Override
	public void terminate() {		
		
		getLogWriter().println(getName() + " is terminating.");
		
		// Remove ourselves from RMI first so the searchers don't reconnect to us.
		this.unbindRMI();
		
		// Send the kill message to each tree searcher in turn.
		for(TreeSearcher searcher : remoteSearchers) {
			try {
				searcher.shouldTerminate();
			} catch (RemoteException e) {
				getLogWriter().println("Failed to kill searcher: " + searcher);
				e.printStackTrace(getLogWriter());
			}
		}
		
	}
	
	/** Tries to convince the searcher to perform the action in the runnable. Trys again if fails
	 * with linear backoff. If it fails, it unregisters the searchers.
	 * @return true if the searcher eventually responds.
	 */
	private boolean tellSearcherToDoSomething(ResiliantSearcherAction action) {
		getLogWriter().println("Attempting to get searcher to do action");
		
		int attempts = 0;
		boolean accepted = false;
		
		while ( ! accepted && attempts < MAX_ACCEPT_MOVE_ATTEMPTS) {
			try {
				
				action.run();
				
				getLogWriter().println("Searcher accepted move");
				
				return true;
			} catch (RemoteException e) {
				// If a searcher fails to accept a move, drop it from the list
				getLogWriter().println(
						"Searcher failed to do action"
								+ attempts + " attempt. Waiting a second then trying again");
				
				e.printStackTrace(getLogWriter());
				
				// Ever tried. Ever failed. No matter. Try again. Fail again. Fail better.
				attempts++;
				accepted = false;
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					getLogWriter().println("Problem sleeping while waiting to try another tellSearcherToDoSomething call");
					e1.printStackTrace(getLogWriter());
				}
			}
		}

		action.failed();
		
		return false;
	}
	
	/** 
	 * Forwards the played move to the remote searchers.
	 * Searchers that respond with an exception will be disconnected.
	 */
	@Override
	public int acceptMove(int p) {
		
		getLogWriter().println(getName() + " acceptMove: " + pointToString(p));
		
		int result = super.acceptMove(p);
		
		if (result == PLAY_OK) {
			
			
			for (Iterator<TreeSearcher> it = remoteSearchers.iterator(); it.hasNext();) {
				final TreeSearcher searcher = it.next();
				final int pF = p;
				
				getLogWriter().println("Trying to get searcher to acceptMove: " + pointToString(p));
				
				// tell the tree searcher to accept the move
				tellSearcherToDoSomething(new ResiliantSearcherAction() {
					public void run() throws RemoteException {
						searcher.acceptMove(opposite(getBoard().getColorToPlay()), pF);
					}
					
					public void failed() {
						unregisterSearcher(searcher);
					}
				});
				
				
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
		
		for (Iterator<TreeSearcher> it = remoteSearchers.iterator(); it.hasNext();) {
			final TreeSearcher searcher = it.next();

			tellSearcherToDoSomething(new ResiliantSearcherAction() {

				@Override
				public void run() throws RemoteException {
					if (!searcher.undo()) {
						throw new RemoteException("Failed to undo!");
					}

				}

				@Override
				public void failed() {
					unregisterSearcher(searcher);

				}
			});
				
		}

		return true;
	}

	@Override
	public void runSearch() {
		beginAcceptingResults();
		for (TreeSearcher searcher : remoteSearchers) {
			
			final TreeSearcher searcherF = searcher;
			
			getLogWriter().println("Telling searcher to run search");
			tellSearcherToDoSomething(new ResiliantSearcherAction() {
				
				@Override
				public void run() throws RemoteException {
					searcherF.beginSearch();
				}
				
				@Override
				public void failed() {
					unregisterSearcher(searcherF);
					
				}
			});
		}
		
		// Wait for as long as we can
		long waitTime = msecToMove > 0 ? msecToMove + latencyFudge : msecToTimeout;
		try {
			searchLock.lock();
			searchDone.await(waitTime, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			getLogWriter().println("Search timed out or was interrupted.");
			e.printStackTrace(getLogWriter());
		} finally {
			searchLock.unlock();
		}
		// Determine the best move from the search results
		if(resultsRemaining > 0) {
			this.terminateSearch();
		}
		stopAcceptingResults();
	}	
	
	/**
	 * Generates moves using the searchers. First, checks the local book to see
	 * if there is a move to play. Then, calls each searcher with the current
	 * search settings and waits for them to reply or time to expire.
	 */
	@Override
	public int bestMove() {
		synchronized (searcherLockObject) {
			getLogWriter().println(getName() + " bestMove called.");
	
			// First check if the last move was a pass and we should pass to win
			if(getBoard().getPasses() > 0 && this.secondPassWouldWinGame()) {
				return PASS;
			}
			
			// Now check the opening book for a move
			if (getOpeningBook() != null) {
				
				int move = getOpeningBook().nextMove(getBoard());
				
				if (move != NO_POINT) {
					getLogWriter().println("Returning opening book move.");
					return move;
				}
			}
			
			// Check to see if we have searchers, if not, play a random move
			// Since searchers may come online at any time, a couple bad moves
			// is better than just resigning
			if(remoteSearchers.size() == 0) {
				getLogWriter().println("No searchers connected. Resigning the game.");
				return Coordinates.RESIGN;
			}
			
			// If we're here, we need to start searching, call up the searchers
			runSearch();

			int move = bestSearchMove();
			Arrays.fill(totalRuns, 0);
			Arrays.fill(totalWins, 0);
			return move;
		}
	}

	private String getName() {
		return this.getClass().getSimpleName() + this.playerIndex;
	}
	
	/** Decides on a move to play from the data received from searchers. */
	protected int bestSearchMove() {
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
	
	@Override
	public boolean secondPassWouldWinGame() {
		getLogWriter().println("Going to query searchers whether to pass.");
		int totalQueried = 0;
		int totalYea = 0;
		for(TreeSearcher searcher : remoteSearchers) {
			try {
				if(searcher.shouldPassToWin()) {
					totalYea++;
				}
				totalQueried++;
			} catch (RemoteException e) {
				getLogWriter().println("Searcher: " + searcher + " failed to respond to pass query.");
				e.printStackTrace(getLogWriter());
			}
		}
		getLogWriter().println("Searchers queried: " + totalQueried + ", total yea: " + totalYea);
		return totalYea >= Math.ceil(totalQueried / 2.0);
	}

	/** Utility method to prepare to accept results */
	public synchronized void beginAcceptingResults() {
		resultsRemaining = remoteSearchers.size();
	}

	public synchronized void stopAcceptingResults() {
		if(resultsRemaining > 0) {
			getLogWriter().println("Results missing from " + resultsRemaining  + " searchers.");
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
		if (key.equals(LOG_FILE_PROPERTY)) {
			try {
				setLogWriter(new PrintWriter(new FileWriter(value, true), true));
				getLogWriter().println("Now logging " + getName() + " debug information to: " + value);
			} catch (Exception e) {
				getLogWriter().println("File " + value + " could not be opened for writing.");
			}
			return;
		}
		if (key.equals(SEARCH_TIMEOUT_PROPERTY)) {
			msecToTimeout = Long.parseLong(value);
		}
		if (key.equals(CLUSTER_PLAYER_INDEX)) {
			if (this.playerIndex >= 0)
				throw new UnsupportedOperationException(String.format("Cannot change already bound cluster player index from %d to %d", 
														this.playerIndex,
														Integer.parseInt(value)));
			
			this.playerIndex = Integer.parseInt(value);
			
			bindRMI();
			
			return;
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
			final double komiF 			 = komi;
			final TreeSearcher searcherF = searcher;
			
			tellSearcherToDoSomething(new ResiliantSearcherAction() {

				@Override
				public void run() throws RemoteException {
					searcherF.setKomi(komiF);

				}

				@Override
				public void failed() {
					unregisterSearcher(searcherF);
				}
			});
		}
	}
	
	@Override
	public void setRemainingTime(int seconds) {
		int movesLeft = max(10, getBoard().getVacantPoints().size() / 2);
		long msec = max(1, (seconds * 1000) / movesLeft);
		try {
			this.setProperty("msec", Long.toString(msec));
		} catch (UnknownPropertyException e) {
			System.err.println("Could not set msec property.");
			e.printStackTrace();
		}
	}
	
	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("final_status_list");
		return result;
	}
	
	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		if(command.equals("final_status_list")) {
			return finalStatusList(arguments.nextToken());
		}
		return super.handleCommand(command, arguments);
	}
	
	@Override
	public String finalStatusList(String status) {
		if(remoteSearchers.size() == 0) return "";
		
		if(resultsRemaining > 0) {
			stopAcceptingResults();
			terminateSearch();
		}
		
		TreeSearcher toQuery = remoteSearchers.get(0);
		try {
			return toQuery.finalStatusList(status);
		} catch (RemoteException e) {
			getLogWriter().println("Searcher 0 failed to respond to finalStatusList.");
			e.printStackTrace(getLogWriter());
			return "";
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
				getLogWriter().println("Could not get total playout count from searcher: " + searcher);
			}
		}
		return totalPlayouts;
	}
	
	@Override
	public int getMillisecondsPerMove() {
		return (int) Math.max(msecToMove, 0);
	}

	protected PrintWriter getLogWriter() {
		return logWriter;
	}

	protected void setLogWriter(PrintWriter logWriter) {
		this.logWriter = logWriter;
	}

	/** Utility method to set a property on all the currently known searchers. */
	protected void setPropertyOnSearchers(String key, String value)
			throws RemoteException {
		for (TreeSearcher searcher : remoteSearchers) {
			searcher.setProperty(key, value);
		}
	}
	
	@Override
	public void terminateSearch() {
		for (TreeSearcher searcher : remoteSearchers) {
			try {
				searcher.terminateSearch();
			} catch (RemoteException e) {
				getLogWriter().println("Got error when trying to stop search on searcher: " + searcher);
				e.printStackTrace(getLogWriter());
			}
		}
	}
}
