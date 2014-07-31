package edu.lclark.orego.mcts;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import edu.lclark.orego.book.OpeningBook;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;

/** Provides various methods specified by interfaces, but does nothing. */
public final class DoNothing implements TreeDescender, TreeUpdater, OpeningBook {

	@Override
	public short bestPlayMove() {
		// Does nothing
		return -1;
	}

	@Override
	public void clear() {
		// Does nothing
	}

	@Override
	public void descend(McRunnable runnable) {
		// Does nothing
	}

	@Override
	public void fakeDescend(McRunnable runnable, short... moves) {
		// Does nothing
	}

	@Override
	public int getBiasDelay() {
		return 0;
	}

	@Override
	public int getGestation() {
		return 0;
	}

	@Override
	public SearchNode getRoot() {
		return null;
	}

	@Override
	public short nextMove(Board board) {
		return NO_POINT;
	}

	@Override
	public void updateForAcceptMove() {
		// Does nothing
	}

	@Override
	public void updateTree(Color winner, McRunnable mcRunnable) {
		// Does nothing
	}

	@Override
	public float searchValue(SearchNode node, short move) {
		return 0;
	}

}
