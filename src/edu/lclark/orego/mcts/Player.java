package edu.lclark.orego.mcts;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.lclark.orego.book.OpeningBook;
import edu.lclark.orego.core.*;
import edu.lclark.orego.score.FinalScorer;
import edu.lclark.orego.time.TimeManager;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.Legality.*;

/** Runs playouts and chooses moves. */
public final class Player {

	private final Board board;

	private OpeningBook book;

	/** @see TreeDescender */
	private TreeDescender descender;

	/** For managing threads. */
	private ExecutorService executor;

	private FinalScorer finalScorer;

	/**
	 * True if the threads should keep running, e.g., because time has not run
	 * out.
	 */
	private boolean keepRunning;

	private boolean usePondering;

	/** Returns the updater for this player. */
	protected TreeUpdater getUpdater() {
		return updater;
	}

	/** Number of seconds left in the game, used for time management */
	private int secondsLeft;

	/** Object used to calculate amount of time used in generating a move. */
	private TimeManager timeManager;

	/** Number of milliseconds to spend on the next move. */
	private int msecPerMove;

	/** For running playouts. */
	private final McRunnable[] runnables;

	/** @see TreeUpdater */
	private TreeUpdater updater;

	/**
	 * @param threads
	 *            Number of threads to run.
	 * @param stuff
	 *            The board and any associated BoardObservers, Mover, etc.
	 */
	public Player(int threads, CopiableStructure stuff) {
		CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		finalScorer = copy.get(FinalScorer.class);
		runnables = new McRunnable[threads];
		for (int i = 0; i < runnables.length; i++) {
			runnables[i] = new McRunnable(this, stuff);
		}
		descender = new DoNothing();
		updater = new DoNothing();
		book = new DoNothing();
	}

	/** Plays at p on this player's board. */
	public Legality acceptMove(short point) {
		stopThreads();
		Legality legality = board.play(point);
		assert legality == OK;
		updater.updateForAcceptMove();
		if (usePondering) {
			startThreads();
		}
		return legality;
	}

	/** Runs the McRunnables for some time and then returns the best move. */
	public short bestMove() {
		short move = book.nextMove(board);
		if (move != NO_POINT) {
			return move;
		}
		timeManager.startNewTurn();
		msecPerMove = timeManager.getTime();
		do {
			startThreads();
			try {
				Thread.sleep(msecPerMove);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
			stopThreads();
			msecPerMove = timeManager.getTime();
		} while (msecPerMove > 0);
		return descender.bestPlayMove();
	}

	public TimeManager getTimeManager() {
		return timeManager;
	}

	public void setRemainingTime(int seconds) {
		timeManager.setRemainingTime(seconds);
	}

	/** Clears the board and does anything else necessary to start a new game. */
	public void clear() {
		stopThreads();
		board.clear();
		descender.clear();
		updater.clear();
	}

	/** Play any moves within the tree (or other structure). */
	public void descend(McRunnable runnable) {
		descender.descend(runnable);
	}

	/** Returns the board associated with this player. */
	public Board getBoard() {
		return board;
	}

	/** Returns the scorer. */
	public FinalScorer getFinalScorer() {
		return finalScorer;
	}

	/** Returns the ith McRunnable. */
	public McRunnable getMcRunnable(int i) {
		return runnables[i];
	}

	public SearchNode getRoot() {
		return updater.getRoot();
	}

	private void startThreads() {
		if (keepRunning) {
			return; // If the threads were already running, don't start them
					// again
		}
		keepRunning = true;
		executor = Executors.newFixedThreadPool(runnables.length);
		for (int i = 0; i < runnables.length; i++) {
			executor.execute(runnables[i]);
		}
		executor.shutdown();
	}

	private void stopThreads() {
		if (!keepRunning) {
			return; // If the threads were not running, don't bother to stop
					// them
		}
		try {
			keepRunning = false;
			executor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** Sets the number of milliseconds to allocate per move. */
	public void setMsecPerMove(int msec) {
		msecPerMove = msec;
	}

	/** Sets which opening book to use. Default is DoNothing. */
	public void setOpeningBook(OpeningBook book) {
		this.book = book;
	}

	public void setTreeDescender(TreeDescender descender) {
		this.descender = descender;

	}

	/** @see TreeUpdater */
	public void setTreeUpdater(TreeUpdater updater) {
		this.updater = updater;
	}

	/** True if McRunnables attached to this Player should keep running. */
	boolean shouldKeepRunning() {
		return keepRunning;
	}

	@Override
	public String toString() {
		return descender.toString();
	}

	/** Incorporate the result of a run in the tree. */
	public void updateTree(Color winner, McRunnable mcRunnable) {
		updater.updateTree(winner, mcRunnable);
	}

	/**
	 * Sets the color to play, used with programs like GoGui to set up initial
	 * stones.
	 */
	public void setColorToPlay(StoneColor stoneColor) {
		board.setColorToPlay(stoneColor);

	}

	public double finalScore() {
		return finalScorer.score();
	}

	/** Returns the number of threads this Player runs. */
	public int getNumberOfThreads() {
		return runnables.length;
	}

	public int getMsecPerMove() {
		return msecPerMove;
	}

	public TreeDescender getDescender() {
		return descender;
	}

	public void usePondering(boolean pondering) {
		this.usePondering = pondering;
	}

	public void setTimeManager(TimeManager time) {
		timeManager = time;
	}

}
