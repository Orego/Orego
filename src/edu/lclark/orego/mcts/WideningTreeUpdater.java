package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.Suggester;
import edu.lclark.orego.util.ShortSet;

public class WideningTreeUpdater implements TreeUpdater {

	private final TranspositionTable table;

	private final Board board;
	
	private final Suggester[] suggesters;
	
	private final int[] weights;

	private static final int NEW_NODE_THRESHOLD = 12;

	private static final int UPDATE_PRIORS_THRESHOLD = 75;

	public WideningTreeUpdater(Board board, TranspositionTable table, Suggester[] suggesters, int[] weights) {
		this.board = board;
		this.table = table;
		this.suggesters = suggesters;
		this.weights = weights;
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
			if (!node.priorsUpdated() && node.getTotalRuns() > UPDATE_PRIORS_THRESHOLD) {
				updatePriors(node);
			}
			long fancyHash = fancyHashes[t + 1];
			SearchNode child = table.findIfPresent(fancyHash);
			if (child == null) {
				short p = history.get(t);
				if (node.getRuns(p) >= NEW_NODE_THRESHOLD) {
					createChildNode(p, node, fancyHash);
					return;
				}
				return;
			}
			node = child;
			winProportion = 1 - winProportion;
		}
	}

	private void updatePriors(SearchNode node) {
		System.out.println("Updating priors");
		for(int i = 0; i < suggesters.length; i++){
			ShortSet moves = suggesters[i].getMoves();
			for(int j = 0; j < moves.size(); j++){
				short p = moves.get(j);
				System.out.println("Updating for " + board.getCoordinateSystem().toString(p));
				node.update(p, weights[i], weights[i]);
			}
		}
		node.setPriorsUpdated(true);

	}

	private void createChildNode(short p, SearchNode parent, long fancyHash) {
		synchronized (table) {
			SearchNode child = table.findOrAllocate(fancyHash);
			if (!parent.hasChild(p)) {
				if (child == null) {
					return; // Table is full
				}
				parent.setHasChild(p);
				table.addChild(parent, child);
			}
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
