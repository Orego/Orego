package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Color;
import edu.lclark.orego.patterns.ShapeTable;

/** Updates the SHAPE tables (and the tree) after a playout. */
public class ShapeUpdater implements TreeUpdater {

	private final TreeUpdater updater;

	public ShapeUpdater(TreeUpdater updater) {
		this.updater = updater;
		// TODO We probably need another field and argument for the SHAPE tables
	}

	@Override
	public void clear() {
		updater.clear();
		// TODO What do we do here? Re-load the SHAPE tables from memory?
		// If so, some sort of "still clean" flag is probably in order.
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
	public void updateForAcceptMove() {
		updater.updateForAcceptMove();
	}

	@Override
	public void updateTree(Color winner, McRunnable runnable) {
		updater.updateTree(winner, runnable);
		// TODO Actual work goes here
	}

	public ShapeTable getTable() {
		// TODO Auto-generated method stub
		return null;
	}

}
