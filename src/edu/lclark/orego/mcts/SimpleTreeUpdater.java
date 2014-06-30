package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.feature.HistoryObserver;
import static edu.lclark.orego.core.NonStoneColor.*;

/** Updates the tree with the results of runs. */
public class SimpleTreeUpdater implements TreeUpdater {

	protected final TranspositionTable table;

	protected final Board board;

	public SimpleTreeUpdater(Board board, TranspositionTable table, int gestation) {
		this.board = board;
		this.table = table;
		this.gestation = gestation;
	}

	@Override
	public void clear() {
		table.sweep();
	}

	/** For testing. Returns the table. */
	TranspositionTable getTable() {
		return table;
	}

	/**
	 * Number of runs required through a move before the corresponding child is
	 * created.
	 */
	protected final int gestation;

	@Override
	public void updateTree(Color winner, McRunnable runnable) {
		int turn = runnable.getTurn();
		SearchNode node = getRoot();
		HistoryObserver history = runnable.getHistoryObserver();
		long[] fancyHashes = runnable.getFancyHashes();
		float winProportion = (winner == board.getColorToPlay()) ? 1 : 0;
		if (winner == VACANT) {
			winProportion = 0.5f;
		}
		for (int t = board.getTurn(); t < turn; t++) {
			node.recordPlayout(winProportion, runnable, t);
			long fancyHash = fancyHashes[t + 1];
			SearchNode child = table.findIfPresent(fancyHash);
			if (child == null) {
				synchronized (table) {
					short p = history.get(t);
					if (node.getRuns(p) >= gestation) {
						child = table.findOrAllocate(fancyHash);
						if (!node.hasChild(p)) {
							if (child == null) {
								return; // Table is full
							}
							node.setHasChild(p);
							table.addChild(node, child);
							return;
						}
					} else {
						return;
					}
				}
			}
			node = child;
			winProportion = 1 - winProportion;
		}
	}

	/** Returns the root node (creating it if necessary). */
	@Override
	public SearchNode getRoot() {
		return table.findOrAllocate(board.getFancyHash());
	}

	/** Returns a human-readable representation of the tree, up to maxDepth. */
	public String toString(int maxDepth) {
		return getRoot().deepToString(board, table, maxDepth);
	}

	@Override
	public int getGestation() {
		return gestation;
	}

	@Override
	public void updateForAcceptMove() {
		SearchNode root = getRoot();
		table.markNodesReachableFrom(root);
		table.sweep();
		root = getRoot();
		assert root != null;
	}

}
