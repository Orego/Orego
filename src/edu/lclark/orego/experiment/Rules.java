package edu.lclark.orego.experiment;

/** Holds player-independent rules, e.g., board size. */
final class Rules {

	final int boardSize;

	final double komi;

	/** In seconds. If there is no time limit, this is a negative number. */
	final int time;

	Rules(int boardSize, double komi, int time) {
		this.boardSize = boardSize;
		this.komi = komi;
		this.time = time;
	}

}
