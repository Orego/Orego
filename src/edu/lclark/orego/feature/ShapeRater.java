package edu.lclark.orego.feature;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.SearchNode;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;

/**
 * This class updates the children of each node with biases based on the SHAPE
 * pattern data.
 */
@SuppressWarnings("serial")
public class ShapeRater implements Rater {

	private final int bias;

	private final Board board;

	private final CoordinateSystem coords;

	private final HistoryObserver history;

	private final int minStones;

	private ShapeTable shapeTable;

	public ShapeRater(Board board, HistoryObserver history,
			ShapeTable shapeTable, int bias, int minStones) {
		this.bias = bias;
		this.board = board;
		this.history = history;
		this.coords = board.getCoordinateSystem();
		this.shapeTable = shapeTable;
		this.minStones = minStones;
	}

	public void setTable(ShapeTable table) {
		shapeTable = table;
	}

	@Override
	public void updateNode(SearchNode node) {
		for (short p : coords.getAllPointsOnBoard()) {
			if (board.getColorAt(p) == VACANT) {
				long hash = PatternFinder.getHash(board, p, minStones,
						history.get(board.getTurn() - 1));
				node.update(p, bias, (int) (bias * shapeTable.getWinRate(hash)));
			}
		}
	}

}
