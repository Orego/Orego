package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.feature.HistoryObserver;

/** Updates the tree with the results of runs. */
public final class SimpleTreeUpdater implements TreeUpdater {

	private final Board board;

	/**
	 * Number of runs required through a move before the corresponding child is
	 * created.
	 */
	private final int gestation;

	private final TranspositionTable table;

	public SimpleTreeUpdater(Board board, TranspositionTable table,
			int gestation) {
		this.board = board;
		this.table = table;
		this.gestation = gestation;
	}

	@Override
	public void clear() {
		table.sweep();
	}

	@Override
	public int getGestation() {
		return gestation;
	}

	/** Returns the root node (creating it if necessary). */
	@Override
	public SearchNode getRoot() {
		return table.findOrAllocate(board.getFancyHash());
	}

	/** For testing. Returns the table. */
	TranspositionTable getTable() {
		return table;
	}

	/** Returns a human-readable representation of the tree, up to maxDepth. */
	public String toString(int maxDepth) {
		return getRoot().deepToString(board, table, maxDepth);
	}

	@Override
	public void updateForAcceptMove() {
		SearchNode root = getRoot();
		table.markNodesReachableFrom(root);
		table.sweep();
		root = getRoot();
		assert root != null;
	}

	@Override
	public void updateTree(Color winner, McRunnable runnable) {
		final int turn = runnable.getTurn();
		SearchNode node = getRoot();
		assert node != null;
		final HistoryObserver history = runnable.getHistoryObserver();
		final long[] fancyHashes = runnable.getFancyHashes();
		float winProportion = winner == board.getColorToPlay() ? 1 : 0;
		if (winner == VACANT) {
			winProportion = 0.5f;
		}
		for (int t = board.getTurn(); t < turn; t++) {
			assert node != null : "Board turn " + board.getTurn()
					+ ", runnable turn: " + turn + ", t: " + t
					+ ", table fullness: " + table.getNodesInUse() + "/"
					+ table.getCapacity() + "="
					+ (((double) table.getNodesInUse()) / table.getCapacity());
			node.recordPlayout(winProportion, runnable, t);
			final long fancyHash = fancyHashes[t + 1];
			synchronized (table) {
				SearchNode child = table.findIfPresent(fancyHash);
				if (child == null) {
					final short p = history.get(t);
					if (node.getRuns(p) >= gestation) {
						child = table.findOrAllocate(fancyHash);
						if (child == null) {
							return; // Table is full
						}
						if (!node.hasChild(p)) {
							node.setHasChild(p);
							table.addChild(node, child);
							return;
						}
					} else {
						return;
					}
				}
				node = child;
			}
			winProportion = 1 - winProportion;
		}
	}

}
