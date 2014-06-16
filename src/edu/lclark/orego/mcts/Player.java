package edu.lclark.orego.mcts;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;

/** Runs playouts and chooses moves. */
public final class Player {

	private final Board board;
	
	/** For managing threads. */
	private final ExecutorService executor;
	
	/** True if the threads should keep running, e.g., because time has not run out. */
	private boolean keepRunning;
	
	/** @see TreeDescender */
	private final TreeDescender generator;
	
	/** Number of milliseconds to spend on the next move. */
	private int millisecondsPerMove;

	/** @see TreeUpdater */
	private TreeUpdater runIncorporator;

	/** For running playouts. */
	private final McRunnable[] runnables;
	
	/**
	 * @param threads Number of threads to run.
	 * @param stuff The board and any associated BoardObservers, Mover, etc.
	 */
	public Player(int threads, CopiableStructure stuff) {
		CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		runnables = new McRunnable[threads];
		for (int i = 0; i < runnables.length; i++) {
			runnables[i] = new McRunnable(this, stuff);
		}
		executor = Executors.newFixedThreadPool(threads);
		runIncorporator = new DoNothing();
		generator = new DoNothing();
	}

	/** Runs the McRunnables for some time and then returns the best move. */
	public short bestMove() {
		keepRunning = true;
		for (int i = 0; i < runnables.length; i++) {
			executor.execute(runnables[i]);
		}
		try {
			Thread.sleep(millisecondsPerMove);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		keepRunning = false;
		return PASS;
	}

	/** Clears the board and does anything else necessary to start a new game. */
	public void clear() {
		board.clear();
		runIncorporator.clear();
	}

	/** Play any moves within the tree (or other structure). */
	public void descend(McRunnable runnable) {
		generator.descend(runnable);
	}

	/** Returns the board associated with this player. */
	public Board getBoard() {
		return board;
	}

	/** Returns the ith McRunnable. */
	public McRunnable getMcRunnable(int i) {
		return runnables[i];
	}

	/** Incorporate the result of a run in the tree. */
	public void updateTree(Color winner, McRunnable mcRunnable) {
		runIncorporator.updateTree(winner, mcRunnable);
	}
	
	/** Sets the number of milliseconds to allocate per move. */
	public void setMillisecondsPerMove(int milliseconds) {
		millisecondsPerMove = milliseconds;
	}

	/** @see TreeUpdater */
	public void setRunIncorporator(TreeUpdater runIncorporator) {
		this.runIncorporator = runIncorporator;
	}
	
	/** True if McRunnables attached to this Player should keep running. */
	boolean shouldKeepRunning() {
		return keepRunning;
	}

}
