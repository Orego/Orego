package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;

/** Simple example of a RunIncorporator. */
public final class TextIncorporator implements RunIncorporator {

	private final StringBuilder data;
	
	public TextIncorporator() {
		data = new StringBuilder();
	}

	@Override
	public void clear() {
		data.setLength(0);
	}

	@Override
	public void incorporateRun(Color winner, McRunnable mcRunnable) {
		data.append(winner + ": ");
		data.append(mcRunnable.getHistoryObserver().toString() + "\n");
	}

	@Override
	public String toString() {
		return data.toString();
	}

}
