package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.NonStoneColor.*;

@SuppressWarnings("serial")
public class ShapeSuggester implements Suggester {

	private final int bias;

	private final Board board;

	private final CoordinateSystem coords;

	private final ShortSet moves;

	private ShapeTable shapeTable;

	private double shapeThreshold;

	public ShapeSuggester(Board board, ShapeTable shapeTable, double shapeThreshold) {
		this(board, shapeTable, shapeThreshold, 0);
	}

	public ShapeSuggester(Board board, ShapeTable shapeTable, double shapeThreshold, int bias) {
		this.bias = bias;
		this.shapeThreshold = shapeThreshold;
		this.board = board;
		this.coords = board.getCoordinateSystem();
		this.shapeTable = shapeTable;
		moves = new ShortSet(coords.getFirstPointBeyondBoard());
	}

	@Override
	public ShortSet getMoves() {
		moves.clear();
		for (short p : coords.getAllPointsOnBoard()) {
			if (board.getColorAt(p) == VACANT) {
				long hash = PatternFinder.getHash(board, p, 24);
				if (shapeTable.getWinRate(hash) > shapeThreshold) {
					moves.add(p);
				}
			}
		}
		return moves;
	}

	public void setTable(ShapeTable shapeTable) {
		this.shapeTable = shapeTable;
	}

	@Override
	public int getBias() {
		return bias;
	}
}
