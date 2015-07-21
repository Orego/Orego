package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.Legality.OK;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.experiment.Logging.log;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.lclark.orego.book.OpeningBook;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.mcts.CopiableStructure;
import edu.lclark.orego.mcts.DoNothing;
import edu.lclark.orego.score.FinalScorer;
import edu.lclark.orego.time.TimeManager;
import edu.lclark.orego.util.ShortList;
import edu.lclark.orego.util.ShortSet;

public class Player {

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
	
	private Population[] populations;

	/** For performing coevolution. */
	private EvoRunnable[] runnables;

	/**
	 * True if the setTimeRemaining method has been called, because a time_left
	 * command was received. If true, use the time manager. Otherwise just
	 * allocate msecPerMove for each move.
	 */
	private boolean timeLeftWasSent;

	/** Object used to calculate amount of time used in generating a move. */
	private TimeManager timeManager;

	/**
	 * @param threads
	 *            Number of threads to run.
	 * @param stuff
	 *            The board and any associated BoardObservers, Mover, etc.
	 */
	public Player(int threads, CopiableStructure stuff) {
		final CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		coords = board.getCoordinateSystem();
		historyObserver = copy.get(HistoryObserver.class);
		finalScorer = copy.get(FinalScorer.class);
		runnables = new EvoRunnable[threads];
		for (int i = 0; i < runnables.length; i++) {
			runnables[i] = new EvoRunnable(this, stuff);
		}
		book = new DoNothing();
		timeLeftWasSent = false;
	}


	/** Plays at p on this player's board. */
	public Legality acceptMove(short point) {
		stopThreads();
		final Legality legality = board.play(point);
		assert legality == OK;
		if (ponder) {
			startThreads();
		}
		return legality;
	}

	/** Runs the EvoRunnables for some time and then returns the best move. */
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
		for (EvoRunnable runnable : runnables) {
			playouts += runnable.getPlayoutsCompleted();
		}
		log("Turn : " + board.getTurn() + " Playouts : " + playouts);
		return runnables[0].vote(board.getColorToPlay());
	}

	/**
	 * Returns true if we can win by passing, assuming that all of our dead
	 * stones are removed and all enemy stones are alive.
	 */
	boolean canWinByPassing() {
		final ShortSet ourDead = findDeadStones(1.0, board.getColorToPlay());
		final Board stonesRemoved = getEvoRunnable(0).getBoard();
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
		populations[BLACK.index()].randomize();
		populations[WHITE.index()].randomize();
		cleanupMode = false;
	}

	public void createPopulations(int populationSize, int individualLength) {
		populations = new Population[] {
				new Population(populationSize, individualLength, coords),
				new Population(populationSize, individualLength, coords)};
		for (int i = 0; i < runnables.length; i++) {
			runnables[i].setPopulations(populations);
		}
	}

	public void setContestants(int contestants) {
		for (int i = 0; i < runnables.length; i++) {
			runnables[i].setContestants(contestants);
		}		
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
		final ShortSet pointsToBias = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		for (int i = 0; i < enemyDeadChains.size(); i++) {
			final short p = enemyDeadChains.get(i);
			if (p == board.getChainRoot(p)) {
				pointsToBias.addAll(board.getLiberties(p));
			}
		}
		// TODO Bias cleanup moves
//		final SearchNode root = getRoot();
//		final int bias = (int) root.getWins(root.getMoveWithMostWins(board
//				.getCoordinateSystem()));
//		for (int i = 0; i < pointsToBias.size(); i++) {
//			root.update(pointsToBias.get(i), bias, bias);
//		}
//		root.setWinningMove(NO_POINT);
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
		final EvoRunnable runnable = getEvoRunnable(0);
		final Board runnableBoard = runnable.getBoard();
		final int runs = 100;
		final ShortSet deadStones = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		final int[] survivals = new int[board.getCoordinateSystem()
				.getFirstPointBeyondBoard()];
		for (int i = 0; i < runs; i++) {
			short passes = board.getPasses();
			runnable.performPlayout(false);
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

	/** Returns the ith McRunnable. */
	public EvoRunnable getEvoRunnable(int i) {
		return runnables[i];
	}

	/** Returns the scorer. */
	public FinalScorer getFinalScorer() {
		return finalScorer;
	}

	HistoryObserver getHistoryObserver() {
		return historyObserver;
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

	int getMsecPerMove() {
		return msecPerMove;
	}

	/** Returns the number of threads this Player runs. */
	int getNumberOfThreads() {
		return runnables.length;
	}

	public int getPlayoutCount() {
		int playouts = 0;
		for (final EvoRunnable runnable : runnables) {
			playouts += runnable.getPlayoutsCompleted();
		}
		return playouts;
	}

	public TimeManager getTimeManager() {
		return timeManager;
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
		// TODO Bias promising moves by injecting them into population
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
		if (latch == null) {
			return;
		}
		try {
			keepRunning = false;
			latch.await();
		} catch (final InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
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
		// TODO Should we re-randomize the population here?
		for (int i = 0; i < movesList.size(); i++) {
			board.play(movesList.get(i));
		}
		if (alreadyRunning) {
			startThreads();
		}
		return true;
	}

	public Population[] getPopulations() {
		return populations;
	}

}
