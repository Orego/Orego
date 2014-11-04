package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.patterns.ShapeTable;

/** Updates the SHAPE tables (and the tree) after a playout. */
public class ShapeUpdater implements TreeUpdater {

	private final TreeUpdater updater;

	private final ShapeTable shapeTable;
	
	public ShapeUpdater(TreeUpdater updater, ShapeTable shapeTable) {
		this.updater = updater;
		this.shapeTable = shapeTable;
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
			int turn = runnable.getTurn();
			boolean win = winner == playerBoard.getColorToPlay();
			StoneColor color = playerBoard.getColorToPlay();
			int t = playerBoard.getTurn();
			long[] localHashes = runnable.getLocalHashes();
			for (; t < turn; t++) {
				long hash = localHashes[t];
//				System.out.println("Before update at " + hash + ": " + shapeTable.getWinRate(hash));
				shapeTable.update(hash, win);
//				System.out.println("After update at " + hash + ": " + shapeTable.getWinRate(hash));
				win = !win;
				color = color.opposite();
			}
		}
	}

	public ShapeTable getTable() {
		return shapeTable;
	}

}
