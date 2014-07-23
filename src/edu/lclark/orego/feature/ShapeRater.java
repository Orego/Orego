package edu.lclark.orego.feature;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.SearchNode;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;

/**
 * This class updates the children of each node with biases based on the 5x5
 * pattern data.
 */
@SuppressWarnings("serial")
public class ShapeRater implements Rater {

	private final Board board;

	private final CoordinateSystem coords;

	private ShapeTable shapeTable;

	/** Patterns with winrate better than this will cause biases to be updated. */
	private double shapeThreshold;

	private final int bias;
	
	private final int shapePatternSize;

	public ShapeRater(Board board, ShapeTable shapeTable, double shapeThreshold, int bias, int shapePatternSize) {
		this.bias = bias;
		this.shapeThreshold = shapeThreshold;
		this.board = board;
		this.coords = board.getCoordinateSystem();
		this.shapeTable = shapeTable;
		this.shapePatternSize = shapePatternSize;
	}

	@Override
	public void updateNode(SearchNode node) {
		for (short p : coords.getAllPointsOnBoard()) {
			if (board.getColorAt(p) == VACANT) {
				long hash = PatternFinder.getHash(board, p, shapePatternSize);
				float winRate = shapeTable.getWinRate(hash);
				if (shapeTable.getWinRate(hash) > shapeThreshold) {
					float winsToAdd = bias * winRate;
					node.update(p, bias, winsToAdd);
				}
			}
		}
	}

	public void setTable(ShapeTable table) {
		shapeTable = table;
	}

}
