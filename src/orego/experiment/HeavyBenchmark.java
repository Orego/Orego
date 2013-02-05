package orego.experiment;

import static orego.core.Board.MAX_MOVES_PER_GAME;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.mcts.McRunnable.MERCY_THRESHOLD;
import orego.core.Board;
import orego.heuristic.HeuristicList;
import ec.util.MersenneTwisterFast;

public class HeavyBenchmark {

	/** Number of playouts to run. */
	public static final int NUMBER_OF_PLAYOUTS = 10000;

	/** Indicates that a playout was cut off early due to the mercy threshold. */
	public static final int PLAYOUT_MERCY = 1;

	/** Indicates that a playout ended normally. */
	public static final int PLAYOUT_OK = 0;

	/** Indicates that a playout ran too long and was aborted. */
	public static final int PLAYOUT_TOO_LONG = 2;

	/** The heuristics to load */
	public static final String HEURISTICS = "Capture@20:Pattern@20";

	/** Random number generator. */
	public static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

	public static void main(String[] args) {
		long startTime, endTime;
		int[] wins = { 0, 0 };
		Board emptyBoard = new Board();
		HeuristicList heursitics = new HeuristicList(HEURISTICS);
		Board currentBoard = new Board();
		startTime = System.currentTimeMillis();
		for (int idx = 0; idx < NUMBER_OF_PLAYOUTS; idx++) {
			currentBoard.copyDataFrom(emptyBoard);
			int result = playout(currentBoard, heursitics);
			switch (result) {
			case PLAYOUT_OK:
				wins[currentBoard.playoutWinner()]++;
				break;
			case PLAYOUT_MERCY:
				wins[currentBoard.approximateWinner()]++;
				break;
			default:
				break;
			}
		}
		endTime = System.currentTimeMillis();
		// Print the results
		System.out.println("Board size: " + BOARD_WIDTH + "x" + BOARD_WIDTH);
		System.out.println("Komi: " + emptyBoard.getKomi());
		long total = endTime - startTime;
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

	private static int playout(Board currentBoard, HeuristicList heuristics) {
		while (true) {
			if (currentBoard.getTurn() >= MAX_MOVES_PER_GAME) {
				return PLAYOUT_TOO_LONG;
			} else if (Math.abs(currentBoard.approximateScore()) > MERCY_THRESHOLD) {
				return PLAYOUT_MERCY;
			} else if (currentBoard.getPasses() == 2) {
				return PLAYOUT_OK;
			}
			heuristics.selectAndPlayOneMove(RANDOM, currentBoard);
		}
	}

}
