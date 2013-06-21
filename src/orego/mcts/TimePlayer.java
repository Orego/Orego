package orego.mcts;

import static java.lang.Math.max;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import orego.play.UnknownPropertyException;
import static orego.core.Coordinates.*;

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
	
	@Override
	public int bestMove() {
		int m = super.bestMove();
		if (thinkLongerWhenBehind && ((getRoot().getWinRate(m) < behindThreshold) || (m == RESIGN))) {
			setMillisecondsPerMove((int) (getMillisecondsPerMove() * longerMultiple));
			return super.bestMove();
		}
		return m;
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
		}
		else {
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
