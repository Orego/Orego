package orego.mcts;

import static java.lang.Math.max;
import orego.play.UnknownPropertyException;
import static orego.core.Coordinates.*;
import static orego.util.Gaussian.*;

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
	private double earlyExitMult = 2.0;

	private static final int THINKING_SLICES = 4;

	@Override
	public int bestMove() {
		// get the total time allocated to this move
		int totalTimeInMs = getMillisecondsPerMove();
				
		if ((compareSecond && compareSecondConf < 1.0)
				|| (compareRest && compareRestConf < 1.0)) {
			// increased the allocated time
			totalTimeInMs *= earlyExitMult;
			// split it into slices
			int timePerIteration = max(1, totalTimeInMs / THINKING_SLICES);
			setMillisecondsPerMove(timePerIteration);

			// execute each slice and stop early if applicable
			for (int i = 0; i < THINKING_SLICES - 1; i++) {
				int best = super.bestMove();
				if ((compareSecond && confidenceBestVsSecondBest() > compareSecondConf)
						|| (compareRest && confidenceBestVsRest() > compareRestConf)) {
					// consider leaving early:
					// only if BEHIND and UNSTABLE-EVAL don't prohibit us
					if ((!behind || !weAreBehind())
							&& (!unstableEval || !isEvaluationUnstable())) {
//						System.err.println("RETURNING EARLY (after " + (i+1) + " iterations) because I am " + confidenceBestVsRest() + " confident.");
						return best;
					}
				}
			}
//			System.err.println("DIDN'T RETURN EARLY because I am only " + confidenceBestVsRest() + " confident!");
		}
//		System.err.println(getMillisecondsPerMove());
		int best = super.bestMove();
		
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
//			System.err.println("Thinking longer since we're only " + confidenceBestVsRest() + " confident in our move: " + pointToString(best));
			maxMultiple = compareRestUnconfMult;
		}

		if (maxMultiple > 0) {
			setMillisecondsPerMove(max(1,
					(int) Math.round(totalTimeInMs * maxMultiple)));
			int newMove = super.bestMove();
//			System.err.println("The new move is: " + pointToString(newMove));
			return newMove;
		} else {
			return best;
		}
	}

	/** Returns true if the winrate of the best move is below behindThreshold. */
	protected boolean weAreBehind() {
		int bestMove = bestStoredMove();
		return getRoot().getWinRate(bestMove) < behindThreshold
				|| bestMove == RESIGN;
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
		
//		System.err.println("RestWinRate = " + restWinRate + ", RestWins = " + restWins + ", RestRuns = " + restRuns + " w/ avg = " + restRunsAverage);
//		System.err.println("BestWinRate = " + bestWinRate + ", BestWins = " + getRoot().getWins(bestMove) + ", BestRuns = " + bestRuns);
		double c = confidence(bestWinRate, bestRuns, restWinRate, restRuns);
//		System.err.println("Conf = " + c);
		return c;
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
		} else if (property.equals("compare-second-unconf")) {
			compareSecondUnconf = Double.parseDouble(value);
		} else if (property.equals("compare-second-unconf-mult")) {
			compareSecondUnconfMult = Double.parseDouble(value);
		} else if (property.equals("compare-rest")) {
			compareRest = true;
		} else if (property.equals("compare-rest-conf")) {
			compareRestConf = Double.parseDouble(value);
		} else if (property.equals("compare-rest-unconf")) {
			compareRestUnconf = Double.parseDouble(value);
		} else if (property.equals("compare-rest-unconf-mult")) {
			compareRestUnconfMult = Double.parseDouble(value);
		} else if (property.equals("early-exit-mult")) {
			earlyExitMult = Double.parseDouble(value);
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

		// don't crash if we're sent < 0 seconds
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

		// never allocate < 1 ms to a move
		if (msPerMove < 1) {
			msPerMove = 1;
		}

		setMillisecondsPerMove(msPerMove);
	}
}
