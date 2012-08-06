package orego.heuristic;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.OFF_BOARD_COLOR;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.*;
import static orego.patterns.Pattern.diagramToNeighborhood;
import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.core.Coordinates;
import orego.patternanalyze.DynamicPattern;
import orego.patterns.ColorSpecificPattern;
import orego.patterns.Cut1Pattern;
import orego.patterns.Pattern;
import orego.patterns.SimplePattern;
import orego.util.BitVector;

public class SquarePatternHeuristic extends PatternHeuristic {

	private int size;

	public SquarePatternHeuristic(int weight, int size) {
		super(weight);
		this.size = size;
	}

	@Override
	public void prepare(Board board) {
		super.prepare(board);
		int move = board.getMove(board.getTurn() - 1);
		int r = Coordinates.row(move);
		int c = Coordinates.column(move);
		for (int i = (0 - size); i <= size; i++) {
			int nr = Math.max(0, r + i);
			nr = Math.min(nr, BOARD_WIDTH-1);
			for (int j = (0 - size); j <= size; j++) {
				int nc = Math.max(0, c + j);
				nc = Math.min(nc, BOARD_WIDTH-1);
				int p = Coordinates.at(nr, nc);
				if (board.getColor(p) == VACANT) {
					char neighborhood = board.getNeighborhood(p);
					if (GOOD_NEIGHBORHOODS[board.getColorToPlay()]
							.get(neighborhood)) {
						recommend(p);
					}
					if (BAD_NEIGHBORHOODS[board.getColorToPlay()]
							.get(neighborhood)) {
						discourage(p);
					}
				}

			}
		}

	}

}
