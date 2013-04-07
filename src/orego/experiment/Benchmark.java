package orego.experiment;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.BOARD_AREA;
import static orego.core.Board.MAX_MOVES_PER_GAME;
import static orego.heuristic.HeuristicList.selectAndPlayUniformlyRandomMove;
import orego.core.Board;
import ec.util.MersenneTwisterFast;

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
			selectAndPlayUniformlyRandomMove(RANDOM, board);
			if (board.getPasses() == 2) {
				return PLAYOUT_OK;
			}
			/** BOARD_AREA / 2 (below) is mercy threshold */
			if (Math.abs(board.approximateScore()) > BOARD_AREA / 2) {
				return PLAYOUT_MERCY;
			}
		} while (true);
	}

}
