package orego.heuristic;

import orego.core.Board;
import orego.util.IntSet;
import static orego.core.Coordinates.*;

/** Discourages playing on the first or second line. */
public class LineHeuristic extends Heuristic {

	private static final IntSet BAD_MOVES = new IntSet(FIRST_POINT_BEYOND_BOARD);

	static {
		for (int p : ALL_POINTS_ON_BOARD) {
			if (line(p) < 3) {
				BAD_MOVES.add(p);
			}
		}
	}

	public LineHeuristic(int weight) {
		super(weight);
		setBadMoves(BAD_MOVES);
	}

	@Override
	public void prepare(Board board) {
		// Does nothing
		// Specifically, does not call super.prepare(), which would clear badMoves
	}

}