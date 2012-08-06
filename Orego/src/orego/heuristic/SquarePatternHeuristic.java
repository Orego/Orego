package orego.heuristic;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;
import orego.core.Board;

/**
 * Just like PatternHeuristic but looks in a square region around the last move.
 */
public class SquarePatternHeuristic extends AbstractPatternHeuristic {

	private int[][] region;

	public SquarePatternHeuristic(int weight) {
		super(weight);
		region = SQUARE_NEIGHBORHOOD[1]; // Default square is 3x3
	}

	@Override
	public void setProperty(String property, String value) {
		super.setProperty(property, value);
		if (property.equals("radius")) {
			region = SQUARE_NEIGHBORHOOD[Integer.valueOf(value)];
		}
	}
	
	@Override
	public void prepare(Board board) {
		super.prepare(board);
		for (int p : region[board.getMove(board.getTurn() - 1)]) {
			if (board.getColor(p) == VACANT) {
				char neighborhood = board.getNeighborhood(p);
				if (GOOD_NEIGHBORHOODS[board.getColorToPlay()]
						.get(neighborhood)) {
					recommend(p);
				}
				if (BAD_NEIGHBORHOODS[board.getColorToPlay()].get(neighborhood)) {
					discourage(p);
				}
			}
		}
	}

}
