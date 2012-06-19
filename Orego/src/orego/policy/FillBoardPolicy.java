package orego.policy;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.patterns.Pattern.*;
import orego.core.Board;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

public class FillBoardPolicy extends Policy {

	public static final int RUNS = 8;

	/**
	 * A 3x3 neighborhood that is fully vacant.
	 */
	public static final char VACANT_NEIGHBORHOOD = diagramToNeighborhood("...\n. .\n...");

	public FillBoardPolicy() {
		this(new RandomPolicy());
	}

	public FillBoardPolicy(Policy fallback) {
		super(fallback);
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		for (int i = 0; i < RUNS; i++) {
			// TODO Do we want to choose from only vacant points? This is not
			// how the paper says to do it. Paper checks if the point is vacant.
			// IntSet vacantPoints = board.getVacantPoints();
			// int vacant = random.nextInt(vacantPoints.size());
			// int p = vacantPoints.get(vacant);
			// if (board.getColor(p) == VACANT) {
			// if (hasAllVacantNeighbors(p, board)
			// && board.playFast(p) == PLAY_OK) {
			// return p;
			// }
			// }
			int r = random.nextInt(ALL_POINTS_ON_BOARD.length);
			int p = ALL_POINTS_ON_BOARD[r];
			if (board.getColor(p) == VACANT) {
				if ((board.getNeighborhood(p) == VACANT_NEIGHBORHOOD)
						&& board.playFast(p) == PLAY_OK) {
					return p;
				}
			}
		}
		return getFallback().selectAndPlayOneMove(random, board);
	}

}
