package orego.mcts;

import static java.lang.Math.max;
import static orego.core.Coordinates.RESIGN;
import static orego.core.Coordinates.getAllPointsOnBoard;
import static orego.core.Coordinates.getFirstPointBeyondBoard;
import static orego.util.Gaussian.Phi;
import orego.play.UnknownPropertyException;
//import java.io.BufferedWriter;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;

public class TimePlayer extends Lgrf2Player {

	/**
	 * The value of timeFormula when we are uniformly distributing our remaining
	 * time.
	 */
	public static final int TIME_FORMULA_UNIFORM = 0;

	/**
	 * The value of timeFormula when we are using Aja's basic formula
	 * (allocating more time to earlier moves).
	 */
	public static final int TIME_FORMULA_BASIC = 1;

	/**
	 * The value of timeFormula when we are using Aja's enhanced formula
	 * (allocating more time to middle-game moves).
	 */
	public static final int TIME_FORMULA_ENHANCED = 2;

	/**
	 * The formula we use for time management: to allocate our time for each
	 * move.
	 */
	private int timeFormula = TIME_FORMULA_UNIFORM;

	/** The constant C to use in the time management formula. */
	private double timeC = 0.20;

	/**
	 * The constant MaxPly to use in the time management formula, where
	 * applicable.
	 */
	private double timeMaxPly = 80.0;

	/**
	 * This is true if we should think for longer when the highest winrate <
	 * behindThreshold.
	 */
	private boolean behind = false;

	/**
	 * This is what we should multiply the time by if we are going to think
	 * longer for a particular move. A reasonable value is 1.0 (think for twice
	 * as long).
	 */
	private double behindMult = 1.0;

	/**
	 * We are considered "behind" if the maximum winrate of any node is less
	 * than this value. A reasonable value is 0.4.
	 */
	private double behindThreshold = 0.4;

	/**
	 * This is true if we should think for longer when the move with the most
	 * wins and the move with the highest win rate are not the same.
	 */
	private boolean unstableEval = false;

	/**
	 * This is what we should multiply the time by if we are going to think
	 * longer due to the unstable-evaluation heuristic. A reasonable value is
	 * 0.5 (think for 50% longer).
	 */
	private double unstableMult = 0.5;

	/**
	 * This is true if we should consider how confident we are that the best
	 * move (A) is better than the second best (B).
	 */
	private boolean compareSecond = false;

	/**
	 * We should stop thinking early if our confidence that A's winrate is
	 * greater than B's winrate is above this value.
	 */
	private double compareSecondConf = 1.0;

	/**
	 * Consider stopping early due to the compare-second-conf heuristic when
	 * there are more than this many milliseconds allocated to the move.
	 */
	private double compareSecondConfDontApplyMax = 5000.0;

	/**
	 * Consider stopping early due to the compare-second-conf heuristic when
	 * there are less than this many milliseconds allocated to the move.
	 */
	private double compareSecondConfDontApplyMin = 800.0;

	/**
	 * This is how much longer we should think when we aren't very confident in
	 * the best move vs. the second best.
	 */
	private double compareSecondUnconfMult = 0.25;

	/**
	 * We should think longer if our confidence that A's winrate is greater than
	 * B's winrate is below this value.
	 */
	private double compareSecondUnconf = 0.0;

	/**
	 * This is true if we should consider how confident we are that the best
	 * move (A) is better than the rest of the moves.
	 */
	private boolean compareRest = false;

	/**
	 * We will stop thinking early if our confidence in the best move vs. the
	 * rest is greater than this value.
	 */
	private double compareRestConf = 1.0;

	/**
	 * Consider stopping early due to the compare-rest-conf heuristic when there
	 * are more than this many milliseconds allocated to the move.
	 */
	private double compareRestConfDontApplyMax = 5000.0;

	/**
	 * Consider stopping early due to the compare-rest-conf heuristic when there
	 * are less than this many milliseconds allocated to the move.
	 */
	private double compareRestConfDontApplyMin = 800.0;

	/**
	 * We will think longer if our confidence in the best move vs. the rest is
	 * less than this value.
	 */
	private double compareRestUnconf = 0.0;

	/**
	 * We will think for this many times longer if we are not confident in the
	 * best move vs. the rest of the moves.
	 */
	private double compareRestUnconfMult = 0.5;

	/**
	 * If we are configured to stop searching early, we will multiply the time
	 * allocated to each move by the formula by this number.
	 */
	private double earlyExitMult = 1.0;

	/**
	 * The number of moves out of the opening book for which we should only
	 * think for one fourth the time.
	 */
	private int quickMovesOutOfBook = 0;

	private int firstMoveOutOfOpeningBook = -22;

	private static final int THINKING_SLICES = 3;

	/**
	 * This keeps track of the playouts per Second
	 */
	private double playoutsPerSec=0;
	
	/**
	 * We will think less if there are previous runs in the root node.
	 */
	private boolean benefitFromPreviousWork = false;

	private final int setRuns = 722;

	@Override
	public int bestMove() {
		
//		System.err.println("Beginning Playouts: " + getRoot().getTotalRuns());
		
		// Get the start time before we run best move
		long startTime = System.currentTimeMillis();
		
		// Get the initial playouts in the tree
		int initialPlayouts = getRoot().getTotalRuns();
		
		// get the total time allocated to this move
		int totalTimeInMs = getMillisecondsPerMove();

		// int earlyMove = -1;

		if ((compareSecond && compareSecondConf < 1.0 && (totalTimeInMs > compareSecondConfDontApplyMax || totalTimeInMs < compareSecondConfDontApplyMin))
				|| (compareRest && compareRestConf < 1.0 && (totalTimeInMs > compareRestConfDontApplyMax || totalTimeInMs < compareRestConfDontApplyMin))
				|| quickMovesOutOfBook > 0) {
			// increased the allocated time
			totalTimeInMs *= earlyExitMult;
			// split it into slices
			int timePerIteration = max(1, totalTimeInMs / THINKING_SLICES);
			setMillisecondsPerMove(timePerIteration);

			// execute each slice and stop early if applicable
			for (int i = 0; i < THINKING_SLICES - 1; i++) {
				int best = super.bestMove();
				
				if (movesOutOfOpeningBook() < quickMovesOutOfBook * 2) {
					updatePlayoutsPerSecond(startTime, initialPlayouts);
					return best;
				}
				
				if ((compareSecond && confidenceBestVsSecondBest() > compareSecondConf)
						|| (compareRest && confidenceBestVsRest() > compareRestConf)) {
					// consider leaving early:
					// only if BEHIND and UNSTABLE-EVAL don't prohibit us
					if ((!behind || !weAreBehind())
							&& (!unstableEval || !isEvaluationUnstable())) {
						updatePlayoutsPerSecond(startTime, initialPlayouts);
						return best;
					}
				}
			}
		}

//		System.err.println(getMillisecondsPerMove());




//		PrintWriter debugFile = null;
//		try {
//			debugFile = new PrintWriter(new BufferedWriter(new FileWriter(
//					"leaveearly.txt", true)));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		int best = super.bestMove();

		updatePlayoutsPerSecond(startTime, initialPlayouts);

//		if (earlyMove != -1) {
//			if (earlyMove == best) {
//				debugFile.println(getTurn() + "," + totalTimeInMs + ",Same");
//				debugFile.flush();
//			} else {
//				debugFile.println(getTurn() + "," + totalTimeInMs
//						+ ",Different");
//				debugFile.flush();
//			}
//		}

//		 debugFile.close();

		// now our time is up. think longer if applicable.
		double maxMultiple = 0.0;

		// check for BEHIND
		if (behind && behindMult > maxMultiple && weAreBehind()) {
			maxMultiple = behindMult;
		}

		// check for UNSTABLE-EVAL
		if (unstableEval && unstableMult > maxMultiple
				&& isEvaluationUnstable()) {
			maxMultiple = unstableMult;
		}

		// check for COMPARE-SECOND
		if (compareSecond && compareSecondUnconfMult > maxMultiple
				&& confidenceBestVsSecondBest() < compareSecondUnconf) {
			maxMultiple = compareSecondUnconfMult;
		}

		// check for COMPARE-REST
		if (compareRest && compareRestUnconfMult > maxMultiple
				&& confidenceBestVsRest() < compareRestUnconf) {
//			System.err.println("Thinking longer since we're only "
//					+ confidenceBestVsRest() + " confident in our move: "
//					+ pointToString(best));
			maxMultiple = compareRestUnconfMult;
		}

		if (maxMultiple > 0) {
			setMillisecondsPerMove(max(1,
					(int) Math.round(totalTimeInMs * maxMultiple)));
			return super.bestMove();
		} else {
			return best;
		}
	}

	protected double confidence(float winrateA, double runsA, float winrateB,
			double runsB) {

//		System.err.println("winrateA = " + winrateA);
//		System.err.println("runsA = " + runsA);
//		System.err.println("winrateB = " + winrateB);
//		System.err.println("runsB = " + runsB);
		double z = (winrateA - winrateB)
				/ Math.sqrt(winrateA * (1 - winrateA) / runsA + winrateB
						* (1 - winrateB) / runsB);
		return Phi(z);
	}

	/**
	 * Returns how confident we are (from 0.0 to 1.0) that the best move has a
	 * higher winrate than the rest of the legal moves.
	 */
	protected double confidenceBestVsRest() {
		// win rate and runs of the best move
		int bestMove = getRoot().getMoveWithMostWins();
		float bestWinRate = getRoot().bestWinRate();
		int bestRuns = getRoot().getRuns(bestMove);

		// runs of the rest of the moves
		int restRuns = getRoot().getTotalRuns() - bestRuns;

		// wins of the rest of the moves
		int restWins = 0;
		for (int p : getAllPointsOnBoard()) {
			if (p != bestMove && getRoot().getWinRate(p) > 0.0) {
				restWins += getRoot().getWins(p);
			}
		}

		float restWinRate = restWins / (float) (restRuns);

//		 System.err.println("RestWinRate = " + restWinRate + ", RestWins = " +
//		 restWins + ", RestRuns = " + restRuns + " w/ avg = " +
//		 restRunsAverage);
//		 System.err.println("BestWinRate = " + bestWinRate + ", BestWins = " +
//		 getRoot().getWins(bestMove) + ", BestRuns = " + bestRuns);
		double c = confidence(bestWinRate, bestRuns, restWinRate, restRuns);
		// System.err.println("Conf = " + c);
		return c;
	}

	/**
	 * Returns how confident we are (from 0.0 to 1.0) that the best move has a
	 * higher winrate than the second best move.
	 * 
	 * By "best" we mean the move with the most wins.
	 */
	protected double confidenceBestVsSecondBest() {
		return confidence(getRoot().bestWinRate(),
				getRoot().getRuns(getRoot().getMoveWithMostWins()), getRoot()
						.getWinRate(moveWithSecondMostWins()), getRoot()
						.getRuns(moveWithSecondMostWins()));
	}

	/**
	 * Returns true if the move with the most wins is not the move with the most
	 * runs.
	 */
	protected boolean isEvaluationUnstable() {
		// Find the move with the most runs
		int moveWithMostRuns = 0;
		int mostRuns = 0;
		for (int i = 0; i < getFirstPointBeyondBoard(); i++) {
			int thisMovesRuns = getRoot().getRuns(i);
			if (thisMovesRuns > mostRuns) {
				moveWithMostRuns = i;
				mostRuns = thisMovesRuns;
			}
		}
		return moveWithMostRuns != getRoot().getMoveWithMostWins();
	}

	/**
	 * Return the number of moves (including the current one) since the opening
	 * book was last used.
	 */
	public int movesOutOfOpeningBook() {
		if (firstMoveOutOfOpeningBook < 0)
			return 0;
		else
			return getTurn() - firstMoveOutOfOpeningBook;
	}

	protected int moveWithSecondMostWins() {
		int bestMove = super.bestStoredMove();
		int secondBestMove = 0;
		int secondMostWins = 0;

		for (int p = 0; p < getFirstPointBeyondBoard(); p++) {
			if (p == bestMove) {
				continue;
			}
			int winsForThisMove = (int) Math.round(getRoot().getWins(p));
			if (winsForThisMove > secondMostWins) {
				secondBestMove = p;
				secondMostWins = winsForThisMove;
			}
		}

		return secondBestMove;
	}

	// System.err.println(pointToString(m) + ": " + getRoot().getWins(m) + "/"
	// + getRoot().getRuns(m) + " = " + getRoot().getWins(m)
	// / getRoot().getRuns(m));
	// System.err.println(pointToString(moveWithSecondMostWins) + ": "
	// + getRoot().getWins(moveWithSecondMostWins) + "/"
	// + getRoot().getRuns(moveWithSecondMostWins) + " = "
	// + getRoot().getWins(moveWithSecondMostWins)
	// / getRoot().getRuns(moveWithSecondMostWins));
	// System.err.println("Total runs: " + getRoot().getTotalRuns());
	// System.err.println(confidence(m, moveWithSecondMostWins)
	// + "% confident.");

	@Override
	public void reset() {
		super.reset();
		firstMoveOutOfOpeningBook = -22;
	}

	@Override
	public void setInOpeningBook(boolean b) {
		if (b == false) {
			firstMoveOutOfOpeningBook = getTurn();
		}
		super.setInOpeningBook(b);
	}

	@Override
	public void setProperty(String property, String value)
			throws UnknownPropertyException {
		if (property.equals("timeformula")) {
			if (value.equals("uniform")) {
				timeFormula = TIME_FORMULA_UNIFORM;
			} else if (value.equals("basic")) {
				timeFormula = TIME_FORMULA_BASIC;
			} else if (value.equals("enhanced")) {
				timeFormula = TIME_FORMULA_ENHANCED;
			} else {
				timeFormula = TIME_FORMULA_UNIFORM;
			}
		} else if (property.equals("c")) {
			timeC = Double.parseDouble(value);
		} else if (property.equals("maxply")) {
			timeMaxPly = Integer.parseInt(value);
		} else if (property.equals("behind")) {
			behind = true;
		} else if (property.equals("behind-threshold")) {
			behindThreshold = Double.parseDouble(value);
		} else if (property.equals("behind-mult")) {
			behindMult = Double.parseDouble(value);
		} else if (property.equals("unstable-eval")) {
			unstableEval = true;
		} else if (property.equals("unstable-mult")) {
			unstableMult = Double.parseDouble(value);
		} else if (property.equals("compare-second")) {
			compareSecond = true;
		} else if (property.equals("compare-second-conf")) {
			compareSecondConf = Double.parseDouble(value);
		} else if (property.equals("compare-second-conf-dont-apply-max")) {
			compareSecondConfDontApplyMax = Double.parseDouble(value);
		} else if (property.equals("compare-second-conf-dont-apply-min")) {
			compareSecondConfDontApplyMin = Double.parseDouble(value);
		} else if (property.equals("compare-second-unconf")) {
			compareSecondUnconf = Double.parseDouble(value);
		} else if (property.equals("compare-second-unconf-mult")) {
			compareSecondUnconfMult = Double.parseDouble(value);
		} else if (property.equals("compare-rest")) {
			compareRest = true;
		} else if (property.equals("compare-rest-conf")) {
			compareRestConf = Double.parseDouble(value);
		} else if (property.equals("compare-rest-conf-dont-apply-max")) {
			compareRestConfDontApplyMax = Double.parseDouble(value);
		} else if (property.equals("compare-rest-conf-dont-apply-min")) {
			compareRestConfDontApplyMin = Double.parseDouble(value);
		} else if (property.equals("compare-rest-unconf")) {
			compareRestUnconf = Double.parseDouble(value);
		} else if (property.equals("compare-rest-unconf-mult")) {
			compareRestUnconfMult = Double.parseDouble(value);
		} else if (property.equals("early-exit-mult")) {
			earlyExitMult = Double.parseDouble(value);
		} else if (property.equals("quick-moves-out-of-book")) {
			quickMovesOutOfBook = Integer.parseInt(value);
		} else {
			super.setProperty(property, value);
		}
	}

	@Override
	public void setRemainingTime(int seconds) {
		/*
		 * Here we decide how much time to spend on each move, given the amount
		 * of time we have left for the game.
		 */

		// to avoid going overtime, try to never have < 10 seconds left
		seconds -= 10;
		
		// don't crash if time left is negative
		if (seconds < 0) {
			seconds = 0;
		}

		int msPerMove;
		switch (timeFormula) {
		case TIME_FORMULA_UNIFORM:
			int movesLeft = max(10,
					(int) (getBoard().getVacantPoints().size() * timeC));
			msPerMove = max(1, (seconds * 1000) / movesLeft);
			break;
		case TIME_FORMULA_BASIC:
			msPerMove = (int) (seconds * 1000 / timeC);
			break;
		case TIME_FORMULA_ENHANCED:
			msPerMove = (int) (seconds * 1000.0 / (timeC + max(timeMaxPly
					- getTurn(), 0)));
			break;
		default:
			msPerMove = 0;
		}

		int timeSaved;
		// benefit from previous work is turned on and 
		// playouts per ms has been set from the previous move
		if (benefitFromPreviousWork && playoutsPerSec > 0) {
			timeSaved = (int) (((getRoot().getTotalRuns() - setRuns) / playoutsPerSec)*1000);
			timeSaved = max(0, timeSaved);
			// Subtract the time we saved
//			System.err.println("Ms Per Move: " + msPerMove);
			msPerMove -= timeSaved;
//			System.err.println("Time saved: " + timeSaved + " ms.");
		}

		// never allocate < 1 ms to a move
		msPerMove = max(1, msPerMove);
		setMillisecondsPerMove(msPerMove);
	}

	/**
	 * Update the playouts per second every move. 
	 * @param startTime
	 * @param initialPlayouts
	 */
	protected void updatePlayoutsPerSecond(long startTime, int initialPlayouts) {
		// Get the final number of playouts
		int finalPlayouts = getRoot().getTotalRuns();
	
//		System.err.println("Ending Playouts: " + finalPlayouts);
		// Get the end time after we run our playouts
		long endTime = System.currentTimeMillis();
	
		// Determine the playouts per second
		playoutsPerSec =  (finalPlayouts - initialPlayouts)
				/ ((endTime - startTime)/1000.0);
	}

	/** Returns true if the winrate of the best move is below behindThreshold. */
	protected boolean weAreBehind() {
		int bestMove = bestStoredMove();
		return getRoot().getWinRate(bestMove) < behindThreshold
				|| bestMove == RESIGN;
	}
}
