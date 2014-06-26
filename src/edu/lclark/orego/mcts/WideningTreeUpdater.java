package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.feature.HistoryObserver;

public class WideningTreeUpdater implements TreeUpdater {

	private final TranspositionTable table;

	private final Board board;
	
	private static final int NEW_NODE_THRESHOLD = 12;

	public WideningTreeUpdater(Board board, TranspositionTable table) {
		this.board = board;
		this.table = table;
	}

	@Override
	public void clear() {
		table.sweep();
	}

	/** For testing. Returns the table. */
	TranspositionTable getTable() {
		return table;
	}

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
					if(node.getRuns(p) < NEW_NODE_THRESHOLD){
						return;
					}
					child = table.findOrAllocate(fancyHash);

					// TODO We probably don't want to create a child on EVERY
					// run
					if (!node.hasChild(p)) {
						if (child == null) {
							return; // Table is full
						}
						node.setHasChild(p);
						table.addChild(node, child);
						// TODO Update priors if child is fresh
						return;
					}
				}
			}
			node = child;
			winProportion = 1 - winProportion;
		}
	}

	/** Returns the root node (creating it if necessary). */
	public SearchNode getRoot() {
		return table.findOrAllocate(board.getFancyHash());
	}

	/** Returns a human-readable representation of the tree, up to maxDepth. */
	public String toString(int maxDepth) {
		return getRoot().deepToString(board, table, maxDepth);
	}

}
