package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.NonStoneColor.*;

/** Suggests moves based on SHAPE tables. */
@SuppressWarnings("serial")
public class ShapeSuggester implements Suggester {

	private final Board board;

	private final CoordinateSystem coords;

	private final ShortSet moves;

	private final ShapeTable shapeTable;

	private final int patternSize;

	/**
	 * Takes a patternSize that is the area of the desired pattern -1 (for the
	 * middle point) e.g. a 3x3 pattern would have patternSize 8.
	 */
	public ShapeSuggester(Board board, ShapeTable shapeTable, int patternSize) {
		this.board = board;
		this.coords = board.getCoordinateSystem();
		this.patternSize = patternSize;
		this.shapeTable = shapeTable;
		moves = new ShortSet(coords.getFirstPointBeyondBoard());
	}

	@Override
	public ShortSet getMoves() {
		moves.clear();
		for (short p : coords.getAllPointsOnBoard()) {
			if (board.getColorAt(p) == VACANT) {
				long hash = PatternFinder.getHash(board, p, patternSize);
				if (shapeTable.getWinRate(hash) > 0.8f) {
					moves.add(p);
				}
			}
		}
		return moves;
	}

}
