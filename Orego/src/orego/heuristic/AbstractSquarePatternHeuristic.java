package orego.heuristic;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;
import orego.core.Board;

/**
 * Just like PatternHeuristic but looks in a square region around the last move.
 */
public abstract class AbstractSquarePatternHeuristic extends AbstractPatternHeuristic {

	private int[][] region;

	public AbstractSquarePatternHeuristic(int weight) {
		super(weight);
	}

	protected void setRegion(int[][] region) {
		this.region = region;
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
		int lastMove = board.getMove(board.getTurn() - 1);
		if (!ON_BOARD[lastMove]) {
			return;
		}
		for (int p : region[lastMove]) {
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
