package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.patterns.PatternFinder;
import edu.lclark.orego.patterns.ShapeTable;

/** Updates the SHAPE tables (and the tree) after a playout. */
public class ShapeUpdater implements TreeUpdater {

	private final TreeUpdater updater;

	private final ShapeTable shapeTable;

	/** For replaying playout moves to find local hashes. */
	private final Board board;

	public ShapeUpdater(TreeUpdater updater, ShapeTable shapeTable, int width) {
		this.updater = updater;
		this.shapeTable = shapeTable;
		board = new Board(width);
	}

	@Override
	public void clear() {
		updater.clear();
		// TODO What do we do here? Re-load the SHAPE tables from memory?
		// If so, some sort of "still clean" flag is probably in order.
	}

	@Override
	public int getGestation() {
		return updater.getGestation();
	}

	@Override
	public SearchNode getRoot() {
		return updater.getRoot();
	}

	@Override
	public void updateForAcceptMove() {
		updater.updateForAcceptMove();
	}

	@Override
	public void updateTree(Color winner, McRunnable runnable) {
		updater.updateTree(winner, runnable);
		if (winner != VACANT) {
			Board playerBoard = runnable.getPlayer().getBoard();
			synchronized (board) {
				board.copyDataFrom(playerBoard);
				HistoryObserver history = runnable.getHistoryObserver();
				int turn = runnable.getTurn();
				boolean win = winner == playerBoard.getColorToPlay();
				int t = playerBoard.getTurn();
				for (; t < turn; t++) {
					short p = history.get(t);
					// TODO Get rid of magic number 3
					long hash = PatternFinder.getHash(board, p, 3,
							history.get(t - 1));
					// TODO Make win a double or float, so we can incorporate
					// ties (winner == VACANT above).
					shapeTable.update(hash, win);
					// System.out.println("Playing " +
					// board.getCoordinateSystem().toString(p));
					board.play(p);
					win = !win;
				}
			}
		}
	}

	public ShapeTable getTable() {
		return shapeTable;
	}

}
