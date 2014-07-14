package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.NonStoneColor.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.LgrfTable;

/** Updates LGRF table after a playout. (Also updates tree.) */
public final class LgrfUpdater implements TreeUpdater {

	private final LgrfTable table;

	private final TreeUpdater updater;

	public LgrfUpdater(TreeUpdater updater, LgrfTable table) {
		this.updater = updater;
		this.table = table;
	}

	@Override
	public void clear() {
		table.clear();
		updater.clear();
	}

	@Override
	public int getGestation() {
		return updater.getGestation();
	}

	@Override
	public SearchNode getRoot() {
		return updater.getRoot();
	}

	/** For testing. */
	LgrfTable getTable() {
		return table;
	}

	@Override
	public void updateForAcceptMove() {
		updater.updateForAcceptMove();
	}

	@Override
	public void updateTree(Color winner, McRunnable runnable) {
		updater.updateTree(winner, runnable);
		HistoryObserver history = runnable.getHistoryObserver();
		if (winner != VACANT) {
			Board playerBoard = runnable.getPlayer().getBoard();
			int turn = runnable.getTurn();
			boolean win = winner == playerBoard.getColorToPlay();
			StoneColor color = playerBoard.getColorToPlay();
			int t = playerBoard.getTurn();
			short penultimate = history.get(t - 2);
			short previous = history.get(t - 1);
			for (; t < turn; t++) {
				short reply = history.get(t);
				table.update(color, win, penultimate, previous, reply);
				win = !win;
				penultimate = previous;
				previous = reply;
				color = color.opposite();
			}
		}
	}

}
