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
	private boolean thinkLongerWhenBehind = false;

	/**
	 * This is what we should multiply the time by if we are going to think
	 * longer for a particular move. A reasonable value is 1.0 (think for twice
	 * as long).
	 */
	private double longerMultiple = 1.0;

	/**
	 * We are considered "behind" if the maximum winrate of any node is less
	 * than this value. A reasonable value is 0.4.
	 */
	private double behindThreshold = 0.4;

	/**
	 * This is true if we should think for longer when the move with the most
	 * wins and the move with the highest win rate are not the same.
	 */
	private boolean unstableEvaluation = false;

	/**
	 * This is what we should multiply the time by if we are going to think
	 * longer due to the unstable-evaluation heuristic. A reasonable value is
	 * 0.5 (think for 50% longer).
	 */
	private double unstableMultiple = 0.5;

	/**
	 * This is true if we should stop thinking early when our confidence is
	 * above confidenceLowerThreshold.
	 */
	private boolean confidenceLess = false;

	/**
	 * We should stop thinking early if our confidence that A's winrate is
	 * greater than B's winrate is above this value.
	 */
	private double confidenceLowerThreshold = 0.6;

	/**
	 * This is true if we should think for confidenceMoreMultiple times longer
	 * if our confidence is below confidenceUpperThreshold.
	 */
	private boolean confidenceMore = false;

	/**
	 * This is how much longer we should think when we aren't very confident in
	 * the best move.
	 */
	private double confidenceMoreMultiple = 0.25;

	/**
	 * We should think longer if our confidence that A's winrate is greater than
	 * B's winrate is below this value.
	 */
	private double confidenceUpperThreshold = 0.5;

	private static final int THINKING_SLICES = 4;

	@Override
	public int bestMove() {
		// get the total time allocated to this move
		int totalTimeInMs = getMillisecondsPerMove();

		// split it into slices
		int timePerIteration = totalTimeInMs / 4;
		setMillisecondsPerMove(timePerIteration);

		// execute each slice and stop early if applicable
		for (int i = 0; i < THINKING_SLICES - 1; i++) {
			int best = super.bestMove();

			if (confidenceLess && weAreVeryConfident()) {
				// consider leaving early:
				// only if TLWB and UE don't prohibit us
				if ((!thinkLongerWhenBehind || !weAreBehind())
						&& (!unstableEvaluation || !isEvaluationUnstable())) {
					return best;
				}
			}
		}

		int best = super.bestMove();

		// now our time is up. think longer if applicable.
		double maxMultiple = 0.0;

		// check for TLWB
		if (thinkLongerWhenBehind && longerMultiple > maxMultiple
				&& weAreBehind()) {
			maxMultiple = longerMultiple;
		}

		// check for UE
		if (unstableEvaluation && unstableMultiple > maxMultiple
				&& isEvaluationUnstable()) {
			maxMultiple = unstableMultiple;
		}

		// check for CONF
		if (confidenceMore && confidenceMoreMultiple > maxMultiple
				&& weArentConfident()) {
			maxMultiple = confidenceMoreMultiple;
		}

		if (maxMultiple > 0) {
			setMillisecondsPerMove(max(1,
					(int) Math.round(totalTimeInMs * maxMultiple)));
			return super.bestMove();
		} else {
			return best;
		}
	}

	/**
	 * Returns true we are very confident that the best move is better than the
	 * 2nd best.
	 **/
	protected boolean weAreVeryConfident() {
		return confidence() > confidenceLowerThreshold;
	}

	/**
	 * Returns true if we aren't confident that the best move is better than the
	 * 2nd best.
	 */
	protected boolean weArentConfident() {
		return confidence() < confidenceUpperThreshold;
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

	/**
	 * Returns how confident we are (from 0.0 to 1.0) that the best move has a
	 * higher winrate than the second best move.
	 * 
	 * By "best" we mean the move with the most wins.
	 */
	protected double confidence() {
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

		float probA = getRoot().getWinRate(bestMove);
		float probB = getRoot().getWinRate(secondBestMove);
		int na = getRoot().getRuns(bestMove);
		int nb = getRoot().getRuns(secondBestMove);
		double z = (probA - probB)
				/ Math.sqrt(probA * (1 - probA) / na + probB * (1 - probB) / nb);
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
		} else if (property.equals("thinklonger")) {
			thinkLongerWhenBehind = true;
		} else if (property.equals("behindthreshold")) {
			behindThreshold = Double.parseDouble(value);
		} else if (property.equals("longermultiple")) {
			longerMultiple = Double.parseDouble(value);
		} else if (property.equals("unstableeval")) {
			unstableEvaluation = true;
		} else if (property.equals("unstablemult")) {
			unstableMultiple = Double.parseDouble(value);
		} else if (property.equals("confidenceless")) {
			confidenceLess = true;
		} else if (property.equals("confidencelow")) {
			confidenceLowerThreshold = Double.parseDouble(value);
		} else if (property.equals("confidencemore")) {
			confidenceMore = true;
		} else if (property.equals("confidenceupper")) {
			confidenceUpperThreshold = Double.parseDouble(value);
		} else if (property.equals("confidencemoremult")) {
			confidenceMoreMultiple = Double.parseDouble(value);
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
