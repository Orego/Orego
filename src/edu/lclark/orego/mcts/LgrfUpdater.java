package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.NonStoneColor.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.feature.LgrfTable;

public final class LgrfUpdater implements TreeUpdater{
	
	private final TreeUpdater updater;
	
	private final LgrfTable table;
	
	public LgrfUpdater(TreeUpdater updater, LgrfTable table){
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

	@Override
	public void updateTree(Color winner, McRunnable runnable) {
		updater.updateTree(winner, runnable);
		HistoryObserver history = runnable.getHistoryObserver();
		if (winner != VACANT) {
			Board playerBoard = runnable.getPlayer().getBoard();
			int turn = runnable.getTurn();
			boolean win = winner == playerBoard.getColorToPlay();
			StoneColor color = playerBoard.getColorToPlay();
			for (int t = playerBoard.getTurn(); t < turn; t++) {
				if(t<2){
					t=1;
					continue;
				}
				short move = history.get(t);
				short antepenultimate = history.get(t-2);
				short previous = history.get(t-1);
				table.update(color, win, antepenultimate, previous, move);
				win = !win;
				antepenultimate = previous;
				previous = move;
				color = color.opposite();
			}
		}
	}

	@Override
	public void updateForAcceptMove() {
		updater.updateForAcceptMove();
	}

}
