package edu.lclark.orego.mcts;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

/** Builds a player. */
@SuppressWarnings("hiding")
public final class PlayerBuilder {

	private int width;
	
	private double komi;
	
	private int threads;
	
	public PlayerBuilder() {
		// Default values
		width = 19;
		komi = 7.5;
		threads = 2;
		msecPerMove = 1000;
	}
	
	public PlayerBuilder boardWidth(int width) {
		this.width = width;
		return this;
	}

	public PlayerBuilder komi(double komi) {
		this.komi = komi;
		return this;
	}

	public PlayerBuilder threads(int threads) {
		this.threads = threads;
		return this;
	}
	
	private int msecPerMove;
	
	public PlayerBuilder msecPerMove(int msec) {
		this.msecPerMove = msec;
		return this;
	}

	private int gestation;
	
	public PlayerBuilder gestation(int gestation) {
		this.gestation = gestation;
		return this;
	}

	/** Creates the Player. */
	public Player build() {
		Player result = new Player(threads, CopiableStructureFactory.useWithPriors(width, komi));
		Board board = result.getBoard();
		CoordinateSystem coords = board.getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(new SimpleSearchNodeBuilder(coords), coords);
		result.setTreeDescender(new UctDescender(board, table));
		TreeUpdater updater = new SimpleTreeUpdater(board, table, gestation);
		result.setTreeUpdater(updater);
		result.setMsecPerMove(msecPerMove);
		return result;
	}

}
