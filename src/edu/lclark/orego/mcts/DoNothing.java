package edu.lclark.orego.mcts;

import edu.lclark.orego.book.OpeningBook;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import static edu.lclark.orego.core.CoordinateSystem.*;

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
	public void updateTree(Color winner, McRunnable mcRunnable) {
		// Does nothing
	}

	@Override
	public int getGestation() {
		return 0;
	}

	@Override
	public int getBiasDelay() {
		return 0;
	}

	@Override
	public SearchNode getRoot() {
		return null;
	}

	@Override
	public void updateForAcceptMove() {
		// Does nothing
	}

	@Override
	public short nextMove(Board board) {
		return NO_POINT;
	}

}
