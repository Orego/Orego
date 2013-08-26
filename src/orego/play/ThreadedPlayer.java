package orego.play;

import static java.lang.Math.*;
import static orego.core.Board.PLAY_OK;
import static orego.core.Coordinates.*;
import static orego.experiment.Debug.debug;

/** A player that runs multiple threads. NOW EXTENDS CGTC*/
public abstract class ThreadedPlayer extends Player {

	/** Milliseconds allocated per move. A value <= 0 indicates no time limit. */
	private int millisecondsPerMove;

	/** If true, think during the other player's turn. */
	private boolean pondering;

	/** Runnables for threads. */
	private Runnable[] runnables;

	/** True if time for this move has not yet expired. */
	private boolean shouldKeepRunning;

	/** Threads wrapped around runnables. */
	private Thread[] threads;

	/** True when threads are running. */
	private boolean threadsRunning;
		
	/** A threaded player with n threads and no pondering. */
	public ThreadedPlayer() {
		threadsRunning = false;
		pondering = false;
		setMillisecondsPerMove(1000);
		threads = new Thread[2];
	}

	@Override
	public int acceptMove(int p) {
		stopThreads();
		int result = super.acceptMove(p);
		if (result == PLAY_OK) {
			updateForAcceptMove(p);
		}
		if (pondering) {
			startThreads();
		}
		return result;
	}

	/** Called at the beginning of startThreads. */
	public abstract void beforeStartingThreads();

	@Override
	public int bestMove() {
		try {
			stopThreads();
			if ((getOpeningBook() != null) && (isInOpeningBook())) {
				int move = getOpeningBook().nextMove(getBoard());
				if (move != NO_POINT) {
					return move;
				} else {
					setInOpeningBook(false);
				}
			}
			runThreads();
		} catch (InterruptedException e) {
			return NO_POINT;
		}
		
		return bestStoredMove();
	}

	/**
	 * Returns the best stored move. Called after threads are stopped by
	 * bestMove().
	 */
	public abstract int bestStoredMove();

	@Override
	public void endGame() {
		stopThreads();
	}

	@Override
	public int getMillisecondsPerMove() {
		return millisecondsPerMove;
	}

	/** Return the number of threads this player uses. */
	public int getNumberOfThreads() {
		return threads.length;
	}

	/** Returns the ith runnable. */
	public Runnable getRunnable(int i) {
		return runnables[i];
	}

	/**
	 * @return true if pondering (thinking during opponent's turn) is turned on.
	 */
	public boolean isPondering() {
		return pondering;
	}

	public void reset() {
		stopThreads();
		super.reset();
		runnables = new Runnable[threads.length];
	}

	/**
	 * Runs the threads until either the allotted time or (for some subclasses)
	 * number of playouts has expired.
	 */
	public void runThreads() throws InterruptedException {
		startThreads();
		if (getMillisecondsPerMove() > 0) {
			Thread.sleep(getMillisecondsPerMove());
			stopThreads();
		} else {
			waitOnThreads();
		}
	}

	/** Sets the amount of time allocated per move. */
	public void setMillisecondsPerMove(int millisecondsPerMove) {
		this.millisecondsPerMove = millisecondsPerMove;
		debug("allocating " + millisecondsPerMove + " milliseconds per move");
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("msec")) {
			setMillisecondsPerMove(Integer.parseInt(value));
		} else if (property.equals("ponder")) {
			pondering = Boolean.parseBoolean(value);
		} else if (property.equals("threads")) {
			threadsRunning = false;
			threads = new Thread[Integer.parseInt(value)];
		} else {
			super.setProperty(property, value);
		}
	}

	@Override
	public void setRemainingTime(int seconds) {
		int movesLeft = max(10, getBoard().getVacantPoints().size() / 2);
		setMillisecondsPerMove(max(1, (seconds * 1000) / movesLeft));
	}

	/** Sets the ith runnable. */
	public void setRunnable(int i, Runnable runnable) {
		runnables[i] = runnable;
	}

	/** Returns true if the time allotted for this move has not yet expired. */
	public boolean shouldKeepRunning() {
		return shouldKeepRunning;
	}

	/** Starts the threads. */
	protected void startThreads() {
		shouldKeepRunning = true;
		if (!threadsRunning) {
			beforeStartingThreads();
			for (int i = 0; i < threads.length; i++) {
				threads[i] = new Thread(runnables[i]);
				threads[i].start();
			}
			threadsRunning = true;
		}
	}

	/** Interrupts the threads and waits for them to stop. */
	protected void stopThreads() {
		if (threadsRunning) {
			shouldKeepRunning = false;
			waitOnThreads();
		}
	}

	/** True if the threads are currently running. */
	protected boolean threadsRunning() {
		return threadsRunning;
	}

	@Override
	public boolean undo() {
		stopThreads();
		boolean result = super.undo();
		if (pondering) {
			startThreads();
		}
		return result;
	}

	/**
	 * Called by acceptMove() after threads are stopped.
	 * 
	 * @param p
	 *            The move made.
	 */
	public abstract void updateForAcceptMove(int p);

	/** Waits for the threads to stop. */
	protected void waitOnThreads() {
		try {
			if (threadsRunning) {
				// Wait for the threads to finish their work
				for (int i = 0; i < threads.length; i++) {
					threads[i].join();
				}
				threadsRunning = false;
			}
		} catch (InterruptedException e) {}
	}

}