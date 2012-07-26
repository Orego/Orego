package orego.experiment;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.KNIGHT_NEIGHBORHOOD;
import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.ON_BOARD;
import static orego.core.Coordinates.PASS;
import static orego.core.Board.MAX_MOVES_PER_GAME;
import static orego.core.Board.PLAY_OK;
import orego.core.Board;
import orego.heuristic.Heuristic;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;
import static orego.mcts.McRunnable.MERCY_THRESHOLD;

/** Simply runs a bunch of playouts to test speed (with one thread and no tree). */
public class Benchmark {

	/** Number of playouts to run. */
	public static final int NUMBER_OF_PLAYOUTS = 10000;

	/** Indicates that a playout was cut off early due to the mercy threshold. */
	public static final int PLAYOUT_MERCY = 1;

	/** Indicates that a playout ended normally. */
	public static final int PLAYOUT_OK = 0;

	/** Indicates that a playout ran too long and was aborted. */
	public static final int PLAYOUT_TOO_LONG = 2;

	/** Random number generator. */
	public static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

	public static void main(String[] args) {
		long before;
		long after;
		int[] wins = { 0, 0 };
		Board original = new Board();
		original.setKomi(7.5);
		Board current = new Board();
		before = System.currentTimeMillis();
		for (int i = 0; i < NUMBER_OF_PLAYOUTS; i++) {
			current.copyDataFrom(original);
			int status = playout(current);
			switch (status) {
			case PLAYOUT_OK:
				wins[current.playoutWinner()]++;
				break;
			case PLAYOUT_MERCY:
				wins[current.approximateWinner()]++;
				break;
			}
		}
		after = System.currentTimeMillis();
		// Print the results
		System.out.println("Board size: " + BOARD_WIDTH + "x" + BOARD_WIDTH);
		System.out.println("Komi: " + original.getKomi());
		long total = after - before;
		System.out.println("Performance:");
		System.out.println("  " + NUMBER_OF_PLAYOUTS + " playouts");
		System.out.println("  " + total / 1000.0 + " seconds");
		System.out.println("  " + ((double) NUMBER_OF_PLAYOUTS) / total
				+ " kpps");
		System.out.println("Black wins = " + wins[BLACK]);
		System.out.println("White wins = " + wins[WHITE]);
		System.out.println("P(black win) = " + ((double) wins[BLACK])
				/ (wins[BLACK] + wins[WHITE]));
	}

	/** Runs one playout. */
	public static int playout(Board board) {
		do {
			if (board.getTurn() >= MAX_MOVES_PER_GAME) {
				// Playout ran out of moves, probably due to superko
				return PLAYOUT_TOO_LONG;
			}
			selectAndPlayOneMove(RANDOM, board);
			if (board.getPasses() == 2) {
				return PLAYOUT_OK;
			}
			if (Math.abs(board.approximateScore()) > MERCY_THRESHOLD) {
				return PLAYOUT_MERCY;
			}
		} while (true);
	}

	// TODO Is the return value necessary?
	// TODO Do we need to pass in the board or the random here?
	public static int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		IntSet vacantPoints = board.getVacantPoints();
		int start = random.nextInt(vacantPoints.size());
		int i = start;
		do {
			int p = vacantPoints.get(i);
			if ((board.getColor(p) == VACANT) && (board.isFeasible(p))
					&& (board.playFast(p) == PLAY_OK)) {
				return p;
			}
			// The magic number 457 is prime and larger than
			// vacantPoints.size().
			// Advancing by 457 therefore skips "randomly" through the array,
			// in a manner analogous to double hashing.
			i = (i + 457) % vacantPoints.size();
		} while (i != start);
		// No legal move; pass
		board.pass();
		return PASS;
	}

}
