package orego.mcts;

import static java.lang.Math.max;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.opposite;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.PASS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;

import orego.heuristic.Heuristic;
import orego.play.UnknownPropertyException;

/**
 * The last-good-reply (with forgetting) player, responding to two moves.
 */
public class Lgrf2Player extends RavePlayer {

	/** Indices are color to play, previous move. */
	private int[][] replies1;

	/** Returns the level 1 reply table. */
	protected int[][] getReplies1() {
		return replies1;
	}
	
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
	private int timeFormula = 0;
	
	/** The constant C to use in the time management formula. */
	private double timeC = 80.0;
	
	/**
	 * The constant MaxPly to use in the time management formula, where
	 * applicable.
	 */
	private double timeMaxPly = 80.0;

	/**
	 * This is true if we should think for longer when the highest winrate <
	 * behindThreshold.
	 */
	private boolean thinkLongerWhenBehind;
	
	/**
	 * This is what we should multiply the time by if we are going to think
	 * longer for a particular move. A reasonable value is 2.0 (think for twice
	 * as long).
	 */
	private double longerMultiple;
	
	/**
	 * We are considered "behind" if the maximum winrate of any node is less
	 * than this value. A reasonable value is 0.4.
	 */
	private double behindThreshold;
	
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

		// think for longer if we are behind (and this time heuristic is
		// enabled)
		if (thinkLongerWhenBehind && getRoot().bestWinRate() < behindThreshold) {
			msPerMove *= longerMultiple;
		}

		setMillisecondsPerMove(msPerMove);

		/*
		 * To ensure we are setting reasonable values, we output a debug
		 * statement, but not to stderr, since this will be redirected to stdout
		 * during experiments and be interpreted as (malformed) GTP responses.
		 */
		File file = new File("timeinfo.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, true);
		} catch (Exception e) {

		}
		PrintStream ps = new PrintStream(fos);

		ps.println(seconds + "s left, " + msPerMove / 1000.0
				+ "s per move, fmla " + timeFormula + ", c=" + timeC
				+ ", maxply=" + timeMaxPly + ", best move="
				+ getRoot().bestWinCountReport());
	}

	/** Indices are color, antepenultimate move, previous move. */
	private int[][][] replies2;

	/** Returns the level 2 replies table. */
	protected int[][][] getReplies2() {
		return replies2;
	}

	public static void main(String[] args) {
		try {
			Lgrf2Player p = new Lgrf2Player();
			p.setProperty("heuristics", "Escape@20:Pattern@20:Capture@20");
			p.setProperty("heuristic.Pattern.numberOfGoodPatterns", "400");
			p.setProperty("threads", "1");
			double[] benchMarkInfo = p.benchmark();
			System.out.println("Mean: " + benchMarkInfo[0] + "\nStd Deviation: "
					+ benchMarkInfo[1]);
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void reset() {
		try {
			super.reset();
			// Create reply tables
			replies1 = new int[NUMBER_OF_PLAYER_COLORS][FIRST_POINT_BEYOND_BOARD];
			replies2 = new int[NUMBER_OF_PLAYER_COLORS][FIRST_POINT_BEYOND_BOARD][FIRST_POINT_BEYOND_BOARD];
			for (int c = BLACK; c <= WHITE; c++) {
				for (int p : ALL_POINTS_ON_BOARD) {
					replies1[c][p] = NO_POINT;
					for (int q : ALL_POINTS_ON_BOARD) {
						replies2[c][p][q] = NO_POINT;
					}
					replies2[c][p][PASS] = NO_POINT;
					replies2[c][p][NO_POINT] = NO_POINT;
				}
				replies1[c][PASS] = NO_POINT;
				replies1[c][NO_POINT] = NO_POINT;
				replies2[c][NO_POINT][PASS] = NO_POINT;
				replies2[c][NO_POINT][NO_POINT] = NO_POINT;
			}
			// Replace McRunnables with LgrfMcRunnables
			for (int i = 0; i < getNumberOfThreads(); i++) {
				setRunnable(i, new LgrfMcRunnable(this, getHeuristics().clone(), replies1, replies2));

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		super.incorporateRun(winner, runnable);
		if (winner != VACANT) {
			int turn = runnable.getTurn();
			int[] moves = runnable.getMoves();
			boolean win = winner == getBoard().getColorToPlay();
			int antepenultimate = getMove(getTurn() - 2);
			int previous = getMove(getTurn() - 1);
			int color = getBoard().getColorToPlay();
			for (int t = getTurn(); t < turn; t++) {
				int move = moves[t];
				if (move != PASS) {
					if (win) {
						replies1[color][previous] = move;
						replies2[color][antepenultimate][previous] = move;
					} else {
						if (replies1[color][previous] == move) {
							replies1[color][previous] = NO_POINT;
						}
						if (replies2[color][antepenultimate][previous] == move) {
							replies2[color][antepenultimate][previous] = NO_POINT;
						}
					}
				}
				win = !win;
				antepenultimate = previous;
				previous = moves[t];
				color = opposite(color);
			}
		}
	}

}
