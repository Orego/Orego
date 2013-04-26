package orego.play;

import static java.lang.Math.*;
import static orego.core.Board.PLAY_OK;
import static orego.core.Coordinates.*;
import static orego.experiment.Debug.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

/** A player that runs multiple threads. */
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
			if (getOpeningBook() != null) {
				int move = getOpeningBook().nextMove(getBoard());
				if (move != NO_POINT) {
					return move;
				}
			}
			runThreads();
		} catch (InterruptedException shouldNotHappen) {
			shouldNotHappen.printStackTrace();
			System.exit(1);
		}
		return bestStoredMove();
	}

	/**
	 * Returns the best stored move. Called after threads are stopped by
	 * bestMove().
	 */
	public abstract int bestStoredMove();

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
		
		/*
		 * Here we implement time management (see Aja's thesis) by deciding how
		 * much time to spend on each move given the amount of time left
		 * (seconds) and based on our estimate of the number of moves remaining.
		 * Note that this goal will only be fully achieved if we receive a
		 * time_left command at every move of the game.
		 */

		int movesLeft = max(10, getBoard().getVacantPoints().size() / 2);
		
		/*
		 * For uniformity, use:
		 *     int msPerMove = max(1, (seconds * 1000) / movesLeft);
		 * to gradually decrease the time per move, use:
		 *     int msPerMove = (seconds * 1000) / C;
		 * to spend more time in the middle game and less at the beginning and end, use:
		 *     int msPerMove = (seconds * 1000) / (movesLeft + C);
		 * where C is some constant.
		 */
		
		int msPerMove = (seconds * 1000) / 100;

		setMillisecondsPerMove(msPerMove);

		/*
		 * To ensure we are setting reasonable values, we output a debug
		 * statement, but not to stderr, since this will be redirected to stdout
		 * during experiments and be interpreted as (malformed) GTP responses.
		 */		
		File file = new File("err.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, true);
		} catch (Exception e) {

		}
		PrintStream ps = new PrintStream(fos);
		ps.println("I was told I have " + seconds
				+ "s left and I set the time per move to " + msPerMove
				/ 1000.0 + "s.");
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
		} catch (InterruptedException shouldNotHappen) {
			shouldNotHappen.printStackTrace();
			System.exit(1);
		}
	}

}