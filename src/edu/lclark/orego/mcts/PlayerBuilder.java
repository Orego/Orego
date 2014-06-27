package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

/** Builds a player. */
@SuppressWarnings("hiding")
public final class PlayerBuilder {

	private int width;
	
	private double komi;
	
	public PlayerBuilder() {
		// Default values
		width = 19;
		komi = 7.5;
	}
	
	public PlayerBuilder boardWidth(int width) {
		this.width = width;
		return this;
	}

	public PlayerBuilder komi(double komi) {
		this.komi = komi;
		return this;
	}

	/** Creates the Player. */
	public Player build() {
		final int milliseconds = 1000;
		final int threads = 2;
		Player result = new Player(threads, CopiableStructureFactory.useWithPriors(width, komi));
		Board board = result.getBoard();
		CoordinateSystem coords = board.getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(new SimpleSearchNodeBuilder(coords), coords);
		result.setTreeDescender(new UctDescender(board, table));
		TreeUpdater updater = new WideningTreeUpdater(board, table);
		result.setTreeUpdater(updater);
		result.setMillisecondsPerMove(milliseconds);
		return result;
	}

}
