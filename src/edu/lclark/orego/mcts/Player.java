package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.experiment.Logging.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.lclark.orego.book.OpeningBook;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.experiment.Logging;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.score.FinalScorer;
import edu.lclark.orego.time.TimeManager;
import edu.lclark.orego.util.ShortList;
import edu.lclark.orego.util.ShortSet;

/** Runs playouts and chooses moves. */
public final class Player {

	private final Board board;

	private OpeningBook book;

	/**
	 * True if we should search for opponent's dead stones and bias moves that
	 * kill them.
	 */
	private boolean cleanupMode;

	private final CoordinateSystem coords;

	/**
	 * True if the polite coup de grace feature is turned on.
	 */
	private boolean coupDeGrace;

	private TreeDescender descender;

	/** For managing threads. */
	private ExecutorService executor;

	private final FinalScorer finalScorer;

	private final HistoryObserver historyObserver;

	/**
	 * True if the threads should keep running, e.g., because time has not run
	 * out.
	 */
	private boolean keepRunning;

	/** Used to verify that all McRunnables have stopped. */
	private CountDownLatch latch;

	/** Number of milliseconds to spend on the next move. */
	private int msecPerMove;

	/** True if we should think during the opponent's turn. */
	private boolean ponder;

	/** For running playouts. */
	private final McRunnable[] runnables;

	/**
	 * True if the setTimeRemaining method has been called, because a time_left
	 * command was received. If true, use the time manager. Otherwise just
	 * allocate msecPerMove for each move.
	 */
	private boolean timeLeftWasSent;

	/** Object used to calculate amount of time used in generating a move. */
	private TimeManager timeManager;

	private TreeUpdater updater;

	/**
	 * @param threads
	 *            Number of threads to run.
	 * @param stuff
	 *            The board and any associated BoardObservers, Mover, etc.
	 */
	public Player(int threads, CopiableStructure stuff) {
		// TODO Is this expensive copying (which includes the transposition and shape tables) necessary?
		// What about in making the McRunnables? Sure, it only happens once, but still.
		final CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		coords = board.getCoordinateSystem();
		historyObserver = copy.get(HistoryObserver.class);
		finalScorer = copy.get(FinalScorer.class);
		runnables = new McRunnable[threads];
		for (int i = 0; i < runnables.length; i++) {
			runnables[i] = new McRunnable(this, stuff);
		}
		descender = new DoNothing();
		updater = new DoNothing();
		book = new DoNothing();
		timeLeftWasSent = false;
	}

	/** Plays at p on this player's board. */
	public Legality acceptMove(short point) {
		stopThreads();
		final Legality legality = board.play(point);
		assert legality == OK;
		updater.updateForAcceptMove();
		if (ponder) {
			startThreads();
		}
		return legality;
	}

	/** Runs the McRunnables for some time and then returns the best move. */
	public short bestMove() {
		stopThreads();
		final short move = book.nextMove(board);
		if (move != NO_POINT) {
			return move;
		}
		if (cleanupMode) {
			if (!findCleanupMoves()) {
				return PASS;
			}
		} else if (board.getPasses() == 1 && coupDeGrace) {
			if (canWinByPassing()) {
				return PASS;
			}
			findCleanupMoves();
		}
		if (!timeLeftWasSent) {
			// No time left signal was received
			startThreads();
			try {
				Thread.sleep(msecPerMove);
			} catch (final InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
			stopThreads();
		} else {
			// Time left signal was received
			timeManager.startNewTurn();
			msecPerMove = timeManager.getMsec();
			do {
				startThreads();
				try {
					Thread.sleep(msecPerMove);
				} catch (final InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
				stopThreads();
				msecPerMove = timeManager.getMsec();
			} while (msecPerMove > 0);
		}
		long playouts = 0;
		for (McRunnable runnable : runnables) {
			playouts += runnable.getPlayoutsCompleted();
		}
		Logging.log("Turn : " + board.getTurn() + " Playouts : " + playouts);
		return descender.bestPlayMove();
	}

	/**
	 * Returns true if we can win by passing, assuming that all of our dead
	 * stones are removed and all enemy stones are alive.
	 */
	boolean canWinByPassing() {
		final ShortSet ourDead = findDeadStones(1.0, board.getColorToPlay());
		final Board stonesRemoved = getMcRunnable(0).getBoard();
		stonesRemoved.copyDataFrom(board);
		stonesRemoved.removeStones(ourDead);
		final double score = finalScorer.score(stonesRemoved);
		if (board.getColorToPlay() == WHITE) {
			if (score < 0) {
				return true;
			}
		} else {
			if (score > 0) {
				return true;
			}
		}
		return false;
	}

	/** Clears the board and does anything else necessary to start a new game. */
	public void clear() {
		stopThreads();
		board.clear();
		descender.clear();
		updater.clear();
		cleanupMode = false;
	}

	/** Play any moves within the tree (or other structure). */
	public void descend(McRunnable runnable) {
		descender.descend(runnable);
	}

	/** Stops any running threads. */
	public void endGame() {
		stopThreads();
	}

	/** @see edu.lclark.orego.score.FinalScorer#score */
	public double finalScore() {
		return finalScorer.score();
	}

	/**
	 * Biases moves that result in clearing opponent's dead chains off the
	 * board. Returns true if any such moves were found.
	 */
	private boolean findCleanupMoves() {
		log("Finding cleanup moves");
		final ShortSet enemyDeadChains = findDeadStones(1.0, board
				.getColorToPlay().opposite());
		log("Dead stones: "
				+ enemyDeadChains.toString(board.getCoordinateSystem()));
		if (enemyDeadChains.size() == 0) {
			return false;
		}
		getRoot().exclude(PASS);
		final ShortSet pointsToBias = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		for (int i = 0; i < enemyDeadChains.size(); i++) {
			final short p = enemyDeadChains.get(i);
			if (p == board.getChainRoot(p)) {
				pointsToBias.addAll(board.getLiberties(p));
			}
		}
		final SearchNode root = getRoot();
		final int bias = (int) root.getWins(root.getMoveWithMostWins(board
				.getCoordinateSystem()));
		for (int i = 0; i < pointsToBias.size(); i++) {
			root.update(pointsToBias.get(i), bias, bias);
		}
		root.setWinningMove(NO_POINT);
		return true;
	}

	/**
	 * Returns a list of stones that don't survive many random playouts.
	 * 
	 * @param threshold
	 *            Portion of games a stone has to survive to be considered
	 *            alive.
	 * @param color
	 *            Color of stones we're examining.
	 */
	public ShortSet findDeadStones(double threshold, StoneColor color) {
		final boolean threadsWereRunning = keepRunning;
		stopThreads();
		// Perform a bunch of runs to see which stones survive
		final McRunnable runnable = getMcRunnable(0);
		final Board runnableBoard = runnable.getBoard();
		final int runs = 100;
		final ShortSet deadStones = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		final int[] survivals = new int[board.getCoordinateSystem()
				.getFirstPointBeyondBoard()];
		for (int i = 0; i < runs; i++) {
			// Temporarily set passes to 0 so that we can run playouts beyond
			// this point
			short passes = board.getPasses();
			board.setPasses((short)0);
			runnable.performMcRun(false);
			board.setPasses(passes);
			for (final short p : board.getCoordinateSystem()
					.getAllPointsOnBoard()) {
				if (runnableBoard.getColorAt(p) == board.getColorAt(p)) {
					survivals[p]++;
				}
			}
		}
		// Gather all of the dead stones into a list to return
		for (final short p : board.getCoordinateSystem().getAllPointsOnBoard()) {
			if (board.getColorAt(p) == color) {
				if (survivals[p] < runs * threshold) {
					deadStones.add(p);
				}
			}
		}
		// Restart the threads if appropriate
		if (threadsWereRunning) {
			startThreads();
		}
		// Return the list of dead stones
		log("Dead stones: " + deadStones.toString(board.getCoordinateSystem()));
		return deadStones;
	}

	/** Returns the board associated with this player. */
	public Board getBoard() {
		return board;
	}

	TreeDescender getDescender() {
		return descender;
	}

	/** Returns the scorer. */
	public FinalScorer getFinalScorer() {
		return finalScorer;
	}

	/** Returns the ith McRunnable. */
	public McRunnable getMcRunnable(int i) {
		return runnables[i];
	}

	int getMsecPerMove() {
		return msecPerMove;
	}

	/** Returns the number of threads this Player runs. */
	int getNumberOfThreads() {
		return runnables.length;
	}

	public int getPlayoutCount() {
		int playouts = 0;
		for (final McRunnable runnable : runnables) {
			playouts += runnable.getPlayoutsCompleted();
		}
		return playouts;
	}

	public SearchNode getRoot() {
		return updater.getRoot();
	}

	public TimeManager getTimeManager() {
		return timeManager;
	}

	/** Returns the updater for this player. */
	TreeUpdater getUpdater() {
		return updater;
	}

	/** Indicate that one McRunnable has stopped. */
	void notifyMcRunnableDone() {
		latch.countDown();
	}

	/** Sets whether we think during the opponent's turn. */
	public void ponder(boolean pondering) {
		this.ponder = pondering;
	}

	/** Sets cleanup mode, as specified in the GTP standard. */
	public void setCleanupMode(boolean cleanup) {
		cleanupMode = cleanup;
	}

	/**
	 * Sets the color to play, used with programs like GoGui to set up initial
	 * stones.
	 */
	public void setColorToPlay(StoneColor stoneColor) {
		board.setColorToPlay(stoneColor);
	}

	/**
	 * Sets whether we should try to capture dead enemy stones after opponent
	 * passes.
	 */
	public void setCoupDeGrace(boolean enabled) {
		coupDeGrace = enabled;
	}

	/** Sets the number of milliseconds to allocate per move. */
	public void setMsecPerMove(int msec) {
		msecPerMove = msec;
	}

	/** Sets which opening book to use. Default is DoNothing. */
	public void setOpeningBook(OpeningBook book) {
		this.book = book;
	}

	/** Handles a time left signal from GTP. */
	public void setRemainingTime(int seconds) {
		timeLeftWasSent = true;
		timeManager.setRemainingSeconds(seconds);
	}

	public void setTimeManager(TimeManager time) {
		timeManager = time;
	}

	public void setTreeDescender(TreeDescender descender) {
		this.descender = descender;

	}

	/** @see TreeUpdater */
	public void setTreeUpdater(TreeUpdater updater) {
		this.updater = updater;
	}

	/** Places standard handicap stones. */
	public void setUpHandicap(int handicapSize) {
		clear();
		board.setUpHandicap(handicapSize);
	}

	/** Places moves read from an SGF game. */
	@SuppressWarnings("boxing")
	public void setUpSgfGame(List<Short> moves) {
		board.clear();
		for (final Short move : moves) {
			if (board.play(move) != OK) {
				throw new IllegalArgumentException("Sgf contained illegal move");
			}
		}
	}

	/** True if McRunnables attached to this Player should keep running. */
	public boolean shouldKeepRunning() {
		return keepRunning;
	}

	/** Starts the McRunnables' threads. */
	private void startThreads() {
		if (keepRunning) {
			return; // If the threads were already running, do nothing
		}
		SearchNode root = getRoot();
		if (!root.biasUpdated()) {
			getMcRunnable(0).copyDataFrom(board);
			root.updateBias(getMcRunnable(0));
		}
		keepRunning = true;
		int n = runnables.length; // # of threads
		latch = new CountDownLatch(n);
		executor = Executors.newFixedThreadPool(n);
		for (int i = 0; i < n; i++) {
			executor.execute(runnables[i]);
		}
		executor.shutdown();
	}

	/** Stops the McRunnables' threads. */
	private void stopThreads() {
		if (!keepRunning) {
			return; // If the threads were not running, do nothing
		}
		try {
			keepRunning = false;
			latch.await();
		} catch (final InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public String toString() {
		return descender.toString();
	}

	/**
	 * Undoes the last move. This is done by clearing the board and replaying
	 * all moves but the last.
	 * 
	 * @return true if undoing succeeded (i.e., it was not the beginning of the
	 *         game).
	 */
	public boolean undo() {
		if (board.getTurn() == 0) {
			return false;
		}
		final boolean alreadyRunning = keepRunning;
		stopThreads();
		final ShortList movesList = new ShortList(board.getCoordinateSystem()
				.getMaxMovesPerGame());
		for (int i = 0; i < historyObserver.size() - 1; i++) {
			movesList.add(historyObserver.get(i));
		}
		// Now replay the moves
		board.clearPreservingInitialStones();
		updater.clear();
		for (int i = 0; i < movesList.size(); i++) {
			board.play(movesList.get(i));
		}
		if (alreadyRunning) {
			startThreads();
		}
		return true;
	}

	/** Incorporate the result of a run in the tree. */
	public void updateTree(Color winner, McRunnable mcRunnable) {
		updater.updateTree(winner, mcRunnable);
	}

	/**
	 * Gets all the stones on the board that live with at least probability
	 * threshold.
	 */
	public ShortSet getLiveStones(double threshold) {
		ShortSet deadStones = findDeadStones(threshold, WHITE);
		deadStones.addAll(findDeadStones(threshold, BLACK));
		ShortSet liveStones = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		for (short p : board.getCoordinateSystem().getAllPointsOnBoard()) {
			if (board.getColorAt(p) != VACANT && !deadStones.contains(p)) {
				liveStones.add(p);
			}
		}
		log("Live stones: " + liveStones.toString(board.getCoordinateSystem()));
		return liveStones;
	}

	/** Returns GoGui information showing search values. */
	@SuppressWarnings("boxing")
	public String goguiSearchValues() {
		// TODO Encapsulate this normalization in a single place, called by all
		// the various gogui methods
		double min = 1.0;
		double max = 0.0;
		for (short p : coords.getAllPointsOnBoard()) {
			if (board.getColorAt(p) == VACANT) {
				double searchValue = descender.searchValue(getRoot(), p);
				if (searchValue > 0) {
					min = Math.min(min, searchValue);
					max = Math.max(max, searchValue);
				}
			}
		}
		String result = "";
		for (short p : coords.getAllPointsOnBoard()) {
			if (getBoard().getColorAt(p) == VACANT) {
				double searchValue = descender.searchValue(getRoot(), p);
				if (searchValue > 0) {
					if (result.length() > 0) {
						result += "\n";
					}
					int green = (int) (255 * (searchValue - min) / (max - min));
					result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
							String.format("#%02x%02x00", 255 - green, green),
							coords.toString(p), coords.toString(p),
							searchValue * 100);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("boxing")
	public String goguiGetWins() {
		// TODO Encapsulate this normalization in a single place, called by all
		// the various gogui methods
		float min = Float.MAX_VALUE;
		float max = 0.0f;
		for (short p : coords.getAllPointsOnBoard()) {
			if (board.getColorAt(p) == VACANT) {
				float wins = getRoot().getWins(p);
				if (wins > 0) {
					min = Math.min(min, wins);
					max = Math.max(max, wins);
				}
			}
		}
		String result = "";
		for (short p : coords.getAllPointsOnBoard()) {
			if (getBoard().getColorAt(p) == VACANT) {
				float wins = getRoot().getWins(p);
				if (wins > 0) {
					if (result.length() > 0) {
						result += "\n";
					}
					int green = (int) (255 * (wins - min) / (max - min));
					result += String.format("COLOR %s %s\nLABEL %s %.0f",
							String.format("#%02x%02x00", 255 - green, green),
							coords.toString(p), coords.toString(p), wins);
				}
			}
		}
		return result;
	}

	@SuppressWarnings("boxing")
	public String goguiGetWinrate() {
		// TODO Encapsulate this normalization in a single place, called by all
		// the various gogui methods
		float min = Float.MAX_VALUE;
		float max = 0.0f;
		for (short p : coords.getAllPointsOnBoard()) {
			if (board.getColorAt(p) == VACANT) {
				float winRate = getRoot().getWinRate(p);
				if (winRate > 0) {
					min = Math.min(min, winRate);
					max = Math.max(max, winRate);
				}
			}
		}
		String result = "";
		for (short p : coords.getAllPointsOnBoard()) {
			if (getBoard().getColorAt(p) == VACANT) {
				float winRate = getRoot().getWinRate(p);
				if (winRate > 0) {
					if (result.length() > 0) {
						result += "\n";
					}
					int green = (int) (255 * (winRate - min) / (max - min));
					result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
							String.format("#%02x%02x00", 255 - green, green),
							coords.toString(p), coords.toString(p),
							(winRate * 100));
				}
			}
		}
		return result;
	}

	@SuppressWarnings("boxing")
	public String goguiGetRuns() {
		// TODO Encapsulate this normalization in a single place, called by all
		// the various gogui methods
		float min = Float.MAX_VALUE;
		float max = 0.0f;
		for (short p : coords.getAllPointsOnBoard()) {
			if (board.getColorAt(p) == VACANT) {
				float runs = getRoot().getRuns(p);
				if (runs > 0) {
					min = Math.min(min, runs);
					max = Math.max(max, runs);
				}
			}
		}
		String result = "";
		for (short p : coords.getAllPointsOnBoard()) {
			if (getBoard().getColorAt(p) == VACANT) {
				float runs = getRoot().getRuns(p);
				if (runs > 0) {
					if (result.length() > 0) {
						result += "\n";
					}
					int green = (int) (255 * (runs - min) / (max - min));
					result += String.format("COLOR %s %s\nLABEL %s %.0f",
							String.format("#%02x%02x00", 255 - green, green),
							coords.toString(p), coords.toString(p), runs);
				}
			}
		}
		return result;
	}

}
