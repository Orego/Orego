package orego.mcts;

import static java.lang.String.format;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import static orego.experiment.Debug.debug;
import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;
import orego.heuristic.Heuristic;
import orego.play.ThreadedPlayer;
import orego.play.UnknownPropertyException;
import orego.util.IntList;

/** A Monte-Carlo player. */
public abstract class McPlayer extends ThreadedPlayer implements StatisticalPlayer {

	/** The maximum playouts a thread will run, if no time limit is set. */
	private long playoutLimit;

	/** Returns the result of benchmark(true). */
	public double[] benchmark() {
		return benchmark(true);
	}

	/**
	 * Runs a 10 second test and returns the speed of the player in thousands of
	 * playouts per second (kpps). If verbose is true, prints additional
	 * information.
	 */
	public double[] benchmark(boolean verbose) {
		int runs = 15;
		//We throw away the first 5 runs
		assert(runs > 5);
		int nruns = runs - 5;
		double[] kppsArray = new double[nruns];
		setMillisecondsPerMove(10000);
		for (int j = 0; j < runs; j++) {
			reset();
			long before = System.currentTimeMillis();
			bestMove();
			long time = System.currentTimeMillis() - before;
			long playouts = 0;
			for (int i = 0; i < getNumberOfThreads(); i++) {
				playouts += ((McRunnable) getRunnable(i))
						.getPlayoutsCompleted();
			}
			double kpps = ((double) playouts) / time;
			if (verbose && j > 4) {
				printAdditionalBenchmarkInfo(kpps, playouts, time);
				System.out.println();
			}
			if (j > 4) {
				kppsArray[j - 5] = kpps;
			}
		}
		double sumOfDeviations = 0.0;
		double sum = 0;
		for (int i = 0; i < nruns; i++) {
			sum += kppsArray[i];
		}
		double mean = sum / nruns;
		for (int i = 0; i < nruns; i++) {
			sumOfDeviations += (kppsArray[i] - mean) * (kppsArray[i] - mean);
		}
		double deviation = Math.sqrt(sumOfDeviations / (runs - 1));
		double[] x = { mean, deviation };
		return x;
	}

	public void printAdditionalBenchmarkInfo(double kpps, long playouts,
			long time) {
		// does nothing
	}

	/**
	 * Returns a color (used by GoGui) corresponding to x. 1.0 maps to green,
	 * 0.0 to red.
	 */
	protected String colorCode(double x) {
		int green = (int) (255 * x);
		return String.format("#%02x%02x00", 255 - green, green);
	}

	/**
	 * Returns a list of stones that survive less than a certain proportion of purely random playouts.
	 * 
	 *  @param threshold 1.0 to find stones that might possibly die, 0.25 to find stones that almost always live.
	 */
	protected IntList getDeadStones(double threshold) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		// Perform runs to see which points survive
		int runs = 100;
		McRunnable r = (McRunnable) getRunnable(0);
		int[] survivals = new int[getExtendedBoardArea()];
		for (int i = 0; i < runs; i++) {
			r.getBoard().copyDataFrom(getBoard());
			r.getBoard().setPasses(0);
			r.playout();
			for (int p : getAllPointsOnBoard()) {
				if (r.getBoard().getColor(p) == getBoard().getColor(p)) {
					survivals[p]++;
				}
			}
		}
		// Clean up data by chain
		IntList result = new IntList(getBoardArea());
		for (int p : getAllPointsOnBoard()) {
			if ((getBoard().getColor(p) != VACANT)
					&& (getBoard().getChainId(p) == p)) {
//				System.out.println(pointToString(p)+" "+survivals[p]);
				if (survivals[p] < runs * threshold) {
					// This chain is not always alive
					int q = p;
					do {
						result.add(q);
						q = getBoard().getChainNextPoint(q);
					} while (q != p);
				}
			}
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}

	/** Play any moves within the tree (or other structure). */
	public abstract void generateMovesToFrontier(McRunnable runnable);

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-mc-playouts");
		result.add("gogui-live-mc-playouts");
		result.add("gogui-win-rates");
		result.add("gogui-heuristics-values");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("gfx/Win rates/gogui-win-rates");
		result.add("gfx/Monte-Carlo playouts/gogui-mc-playouts");
		result.add("none/Live Monte-Carlo playouts/gogui-live-mc-playouts");
		result.add("gfx/Heuristic values/gogui-heuristics-values");
		return result;
	}

	/**
	 * Returns the max number of playouts each thread will run. A playoutLimit
	 * <= 0 indicates no limit.
	 */
	public long getPlayoutLimit() {
		return playoutLimit;
	}

	/** Returns the top level number of playouts through point p. */
	public abstract long getPlayouts(int p);

	/** Returns the win rate for the current color to play at point p. */
	public abstract double getWinRate(int p);

	/** Returns the number of wins for the current color to play at point p. */
	public abstract double getWins(int p);
	
	/**
	 * Display heuristics on the board.
	 */
	protected String goguiHeuristicsValues(){
		String result = "INFLUENCE";
		int[] heuristicsValues = new int[getFirstPointBeyondBoard()];
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < getHeuristics().size(); i++) {
			getHeuristics().get(i).prepare(getBoard());
		}
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				heuristicsValues[p] = getHeuristics().moveRating(p, getBoard());
			}
			min = Math.min(min, heuristicsValues[p]);
			max = Math.max(max, heuristicsValues[p]);
		}
		// Display win rates as colors and percentages
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				if (result.length() > 0) {
					result += "\n";
				}
				result += String.format("COLOR %s %s\nLABEL %s %.0f",
						colorCode((double) (heuristicsValues[p]-min) / (max-min)),
						pointToString(p), pointToString(p), (double) heuristicsValues[p]);
			}
		}
		return result;
	}

	/** Returns GoGui information showing playout distribution and win rates. */
	protected String goguiPlayouts() {
		// Find the max playouts of any move
		long max = 0;
		for (int p : getAllPointsOnBoard()) {
			long playouts = getPlayouts(p);
			if (playouts > max) {
				max = playouts;
			}
		}
		// Display proportional playouts through each move
		String result = "INFLUENCE";
		for (int p : getAllPointsOnBoard()) {
			result += format(" %s %.3f", pointToString(p), getPlayouts(p)
					/ (double) max);
		}
		// Label all moves with number of playouts
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				if (getWinRate(p) > 0) {
					result += format("\nLABEL %s %d", pointToString(p),
							getPlayouts(p));
				}
			}
		}
		// Highlight best move
		// TODO This causes some (but not all) infeasible moves to be excluded
		// -- why?
		int best = bestStoredMove();
		if (isOnBoard(best)) {
			result += "\nCOLOR green " + pointToString(best);
		}
		return result;
	}

	/** Returns GoGui information showing win rates as colors and percentages. */
	public String goguiWinRates() {
		// Find the maximum and minimum win rates on the board, ignoring
		// occupied points
		double max = 0, min = 1;
		double maxWins = 0;
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getWinRate(p);
				// Excluded moves have negative win rates
				if (winRate > 0) {
					max = Math.max(max, winRate);
					min = Math.min(min, winRate);
					maxWins = Math.max(maxWins, getWins(p));
				}
			}
		}
		// Display proportional wins through each move
		String result = "INFLUENCE";
		for (int p : getAllPointsOnBoard()) {
			if (getWinRate(p) > 0) {
				result += format(" %s %.3f", pointToString(p), getWins(p)
						/ (double) maxWins);
			}
		}
		// Display win rates as colors and percentages
		for (int p : getAllPointsOnBoard()) {
			if (getBoard().getColor(p) == VACANT) {
				double winRate = getWinRate(p);
				if (winRate > 0) {
					assert winRate <= 1.0;
					if (result.length() > 0) {
						result += "\n";
					}
					result += String.format("COLOR %s %s\nLABEL %s %.0f%%",
							colorCode(Math.max((winRate - min), 0.0)
									/ (max - min)), pointToString(p),
							pointToString(p), winRate * 100);
				}
			}
		}
		return result;
	}

	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		boolean threadsWereRunning = threadsRunning();
		stopThreads();
		String result = null;
		if (command.equals("gogui-heuristics-values")) {
			result = goguiHeuristicsValues();
		} else if (command.equals("gogui-mc-playouts")) {
			result = goguiPlayouts();
		} else if (command.equals("gogui-live-mc-playouts")) {
			long oldValue;
			boolean isMilliseconds = (getMillisecondsPerMove() != -1);
			if (isMilliseconds) {
				oldValue = getMillisecondsPerMove();
			} else {
				oldValue = getPlayoutLimit();
			}
			setMillisecondsPerMove(1000);
			for (int i = 0; i < 10; i++) {
				bestMove();
				System.err.println("gogui-gfx: \n" + goguiPlayouts() + "\n");
			}
			if (isMilliseconds) {
				setMillisecondsPerMove((int) oldValue);
			} else {
				setPlayoutLimit(oldValue);
			}
			System.err.println("gogui-gfx: CLEAR");
			result = "";
		} else if (command.equals("gogui-win-rates")) {
			result = goguiWinRates();
		} else {
			result = super.handleCommand(command, arguments);
		}
		if (threadsWereRunning) {
			startThreads();
		}
		return result;
	}

	/** Incorporate the result of a run in the tree. */
	public abstract void incorporateRun(int winner, McRunnable runnable);

	/**
	 * Returns true if we would win after removing OUR dead stones.
	 */
	public boolean secondPassWouldWinGame() {
		Board after = new Board();
		after.copyDataFrom(getBoard());
		IntList dead = getDeadStones(1.0);
		for (int p : getAllPointsOnBoard()) {
			if ((getBoard().getColor(p) == getBoard().getColorToPlay())
					&& dead.contains(p)) {
				after.removeStone(p);
			}
		}
		return after.finalWinner() == getBoard().getColorToPlay();
	}

	@Override
	public void setMillisecondsPerMove(int millisecondsPerMove) {
		assert millisecondsPerMove > 0 : "Cannot allocate less than 1 millisecond per move.";
		playoutLimit = -1;
		super.setMillisecondsPerMove(millisecondsPerMove);
	}

	/** Set the max number of playouts a thread will run. */
	public void setPlayoutLimit(long oldValue) {
		assert oldValue >= 0 : "Cannot allocate less than 0 playouts per move.";
		super.setMillisecondsPerMove(-1);
		this.playoutLimit = oldValue;
		debug("playout limit set to " + oldValue + " playouts per thread");
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("playouts")) {
			setPlayoutLimit(Integer.parseInt(value));
		} else {
			super.setProperty(property, value);
		}
	}

	/** Returns a human-readable String indicating black's estimated success. */
	protected abstract String winRateReport();

}