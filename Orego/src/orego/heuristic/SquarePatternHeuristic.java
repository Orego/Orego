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

/**
 * Just like PatternHeuristic but looks in a 3x3 (size of 0), 5x5 (size of 1)...
 * area around the last move.
 * 
 */
public class SquarePatternHeuristic extends PatternHeuristic {

	private int size;

	public SquarePatternHeuristic(int weight, int size) {
		super(weight);
		this.size = size;
	}

	@Override
	public void setProperty(String property, String value) {
		super.setProperty(property, value);
		if (property.equals("size")) {
			this.size = Integer.valueOf(value);
		}
	}
	
	@Override
	public void prepare(Board board) {
		super.prepare(board);
		int move = board.getMove(board.getTurn() - 1);
		for (int p : SQUARE_NEIGHBORHOOD[size][move]) {
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
