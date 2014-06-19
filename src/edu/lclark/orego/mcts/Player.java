package edu.lclark.orego.mcts;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.lclark.orego.core.*;
import edu.lclark.orego.score.ChineseFinalScorer;
import edu.lclark.orego.score.Scorer;
import static edu.lclark.orego.core.Legality.*;

/** Runs playouts and chooses moves. */
public final class Player {

	private final Board board;
	
	/** @see TreeDescender */
	private TreeDescender descender;
	
	/** For managing threads. */
	private ExecutorService executor;
	
	/** True if the threads should keep running, e.g., because time has not run out. */
	private boolean keepRunning;
	
	/** Number of milliseconds to spend on the next move. */
	private int millisecondsPerMove;

	/** For running playouts. */
	private final McRunnable[] runnables;

	/** @see TreeUpdater */
	private TreeUpdater updater;
	
	private Scorer finalScorer;
	
	/**
	 * @param threads Number of threads to run.
	 * @param stuff The board and any associated BoardObservers, Mover, etc.
	 */
	public Player(int threads, CopiableStructure stuff) {
		CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		
		//TODO Create separate interfaces for final and playout scorers. 
		finalScorer = copy.get(ChineseFinalScorer.class);
		runnables = new McRunnable[threads];
		for (int i = 0; i < runnables.length; i++) {
			runnables[i] = new McRunnable(this, stuff);
		}
		descender = new DoNothing();
		updater = new DoNothing();
	}

	/** Plays at p on this player's board. */
	public Legality acceptMove(short point) {
		Legality legality = board.play(point);
		assert legality == OK;
		return legality;
	}

	/** Runs the McRunnables for some time and then returns the best move. */
	public short bestMove() {
		runThreads();
		return descender.bestPlayMove();
	}

	/** Clears the board and does anything else necessary to start a new game. */
	public void clear() {
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

	/** Returns the ith McRunnable. */
	public McRunnable getMcRunnable(int i) {
		return runnables[i];
	}

	// TODO Divide this into startThreads and stopThreads for pondering
	/** Runs the threads. */
	private void runThreads() {
		keepRunning = true;
		executor = Executors.newFixedThreadPool(runnables.length);
		for (int i = 0; i < runnables.length; i++) {
			executor.execute(runnables[i]);
		}
		executor.shutdown();
		try {
			Thread.sleep(millisecondsPerMove);
			keepRunning = false;
			executor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/** Sets the number of milliseconds to allocate per move. */
	public void setMillisecondsPerMove(int milliseconds) {
		millisecondsPerMove = milliseconds;
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

	/** Sets the color to play, used with programs like GoGui to set up initial stones. */
	public void setColorToPlay(StoneColor stoneColor) {
		board.setColorToPlay(stoneColor);
		
	}

	public double finalScore() {
		return finalScorer.score();
	}

}
