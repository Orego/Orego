package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.util.ShortSet;

public class RaveTreeUpdater extends SimpleTreeUpdater{
	
	private final ShortSet playedMoves;

	public RaveTreeUpdater(Board board, TranspositionTable table, int gestation) {
		super(board, table, gestation);
		playedMoves = new ShortSet(board.getCoordinateSystem().getFirstPointBeyondBoard());		
	}
	
	@Override
	public void updateTree(Color winner, McRunnable runnable){
		int turn = runnable.getTurn();
		RaveNode node = (RaveNode) getRoot();
		HistoryObserver history = runnable.getHistoryObserver();
		long[] fancyHashes = runnable.getFancyHashes();
		float winProportion = (winner == board.getColorToPlay()) ? 1 : 0;
		if (winner == VACANT) {
			winProportion = 0.5f;
		}
		for (int t = board.getTurn(); t < turn; t++) {
			node.recordPlayout(winProportion, runnable, t, playedMoves);
			long fancyHash = fancyHashes[t + 1];
			RaveNode child = (RaveNode) table.findIfPresent(fancyHash);
			if (child == null) {
				synchronized (table) {
					short p = history.get(t);
					if (node.getRuns(p) >= gestation) {
						child = (RaveNode) table.findOrAllocate(fancyHash);
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

}
