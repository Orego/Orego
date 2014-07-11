package edu.lclark.orego.mcts;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.lclark.orego.book.OpeningBook;
import edu.lclark.orego.core.*;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.score.FinalScorer;
import edu.lclark.orego.time.TimeManager;
import edu.lclark.orego.util.ShortList;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.Legality.*;

/** Runs playouts and chooses moves. */
public final class Player {

	private final Board board;

	private OpeningBook book;

	/**
	 * Indicates whether to search for opponent's dead stones and bias moves
	 * that kill them.
	 */
	private boolean cleanupMode;

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

	/** Number of milliseconds to spend on the next move. */
	private int msecPerMove;

	/** For running playouts. */
	private final McRunnable[] runnables;

	/** Object used to calculate amount of time used in generating a move. */
	private TimeManager timeManager;

	/** @see TreeUpdater */
	private TreeUpdater updater;
	
	private HistoryObserver historyObserver;

	private boolean usePondering;

	private boolean coupDeGrace;

	/**
	 * @param threads
	 *            Number of threads to run.
	 * @param stuff
	 *            The board and any associated BoardObservers, Mover, etc.
	 */
	public Player(int threads, CopiableStructure stuff) {
		CopiableStructure copy = stuff.copy();
		board = copy.get(Board.class);
		historyObserver = copy.get(HistoryObserver.class);
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
		stopThreads();
		short move = book.nextMove(board);
		if (move != NO_POINT) {
			return move;
		}
		if (cleanupMode) {
			if (cleanup() == PASS) {
				return PASS;
			}
		} else if (board.getPasses() == 1 && coupDeGrace) {
			if (passIfAhead()) {
				return PASS;
			}
			cleanup();
		}
		timeManager.startNewTurn();
		msecPerMove = timeManager.getMsec();
		do {
			startThreads();
			try {
				Thread.sleep(msecPerMove);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
			stopThreads();
			msecPerMove = timeManager.getMsec();
		} while (msecPerMove > 0);
		// System.err.println("Done thinking");
		// System.err.println("Move will be " + descender.bestPlayMove());
		return descender.bestPlayMove();
	}

	private boolean passIfAhead() {
		double score = finalScorer.score();
		int ourDead = findDeadStones(1.0, board.getColorToPlay()).size();
		if (board.getColorToPlay() == WHITE) {
			score += 2 * ourDead;
			if (score < 0) {
				return true;
			}
		} else {
			score -= 2 * ourDead;
			if (score > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Biases moves that result in clearing opponent's dead chains off the
	 * board. Returns PASS if there are no moves to bias.
	 */
	private short cleanup() {
		ShortSet enemyDeadChains = findDeadStones(1.0, board.getColorToPlay()
				.opposite());
		if (enemyDeadChains.size() == 0) {
			return PASS;
		}
		getRoot().exclude(PASS);
		ShortSet pointsToBias = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		for (int i = 0; i < enemyDeadChains.size(); i++) {
			short p = enemyDeadChains.get(i);
			if (p == board.getChainRoot(p)) {
				pointsToBias.addAll(board.getLiberties(p));
			}
		}
		SearchNode root = getRoot();
		int bias = (int) root.getWins(root.getMoveWithMostWins(board
				.getCoordinateSystem()));
		for (int i = 0; i < pointsToBias.size(); i++) {
			// System.out.println(bias);
			root.update(pointsToBias.get(i), bias, bias);
		}
		return -1;
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
	
	public void endGame(){
		stopThreads();
	}

	public double finalScore() {
		return finalScorer.score();
	}

	private ShortSet findDeadStones(double threshold, StoneColor color) {
		boolean threadsWereRunning = keepRunning;
		stopThreads();

		McRunnable runnable = getMcRunnable(0);
		Board runnableBoard = runnable.getBoard();
		int runs = 100;
		ShortSet deadChains = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		int[] survivals = new int[board.getCoordinateSystem()
				.getFirstPointBeyondBoard()];
		for (int i = 0; i < runs; i++) {
			runnableBoard.copyDataFrom(board);
			runnableBoard.setPasses((short) 0);
			runnable.playout();
			for (short p : board.getCoordinateSystem().getAllPointsOnBoard()) {
				if (runnableBoard.getColorAt(p) == board.getColorAt(p)) {
					survivals[p]++;
				}
			}
		}

		for (short p : board.getCoordinateSystem().getAllPointsOnBoard()) {
			if (board.getColorAt(p) == color) {
				if (survivals[p] < runs * threshold) {
					deadChains.add(p);
				}
			}
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return deadChains;
	}

	/** Returns the board associated with this player. */
	public Board getBoard() {
		return board;
	}

	public TreeDescender getDescender() {
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

	public int getMsecPerMove() {
		return msecPerMove;
	}

	/** Returns the number of threads this Player runs. */
	public int getNumberOfThreads() {
		return runnables.length;
	}

	public SearchNode getRoot() {
		return updater.getRoot();
	}

	public TimeManager getTimeManager() {
		return timeManager;
	}

	/** Returns the updater for this player. */
	protected TreeUpdater getUpdater() {
		return updater;
	}

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

	public void setRemainingTime(int seconds) {
		timeManager.setRemainingTime(seconds);
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

	/** True if McRunnables attached to this Player should keep running. */
	public boolean shouldKeepRunning() {
		return keepRunning;
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

	@Override
	public String toString() {
		return descender.toString();
	}

	/** Incorporate the result of a run in the tree. */
	public void updateTree(Color winner, McRunnable mcRunnable) {
		updater.updateTree(winner, mcRunnable);
	}

	public void usePondering(boolean pondering) {
		this.usePondering = pondering;
	}

	public int getPlayoutCount() {
		int playouts = 0;
		for(McRunnable runnable : runnables){
			playouts += runnable.getPlayoutsCompleted();
		}
		return playouts;
	}

	public boolean undo() {
		if(board.getTurn() == 0){
			return false;
		}
		boolean alreadyRunning = keepRunning;
		stopThreads();
		ShortList movesList = new ShortList(board.getCoordinateSystem().getFirstPointBeyondBoard());
		for(int i = 0; i<historyObserver.size() -1; i++){
			movesList.add(historyObserver.get(i));
		}
		
		board.clearPreservingInitialStones();
		for(int i = 0; i<movesList.size(); i++){
			board.play(movesList.get(i));
		}
		if(alreadyRunning){
			startThreads();
		}
		return true;
	}

	@SuppressWarnings("boxing")
	public void setUpSgfGame(List<Short> moves) {
		board.clear();
		for(Short move : moves){
			if(board.play(move) != OK){
				throw new IllegalArgumentException("Sgf contained illegal move");
			}
		}
		
	}

	public void setUpHandicap(int handicapSize) {
		clear();
		board.setUpHandicap(handicapSize);
	}

}
