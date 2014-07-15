package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;

/** Stores runs as human-readable text. Mainly for testing. */
public final class TextUpdater implements TreeUpdater {

	private final StringBuilder data;

	public TextUpdater() {
		data = new StringBuilder();
	}

	@Override
	public void clear() {
		data.setLength(0);
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
	public String toString() {
		return data.toString();
	}

	@Override
	public void updateForAcceptMove() {
		// Does nothing
	}

	@Override
	public void updateTree(Color winner, McRunnable mcRunnable) {
		data.append(winner + ": ");
		data.append(mcRunnable.getHistoryObserver().toString() + "\n");
	}

}
