package edu.lclark.orego.experiment;

/** Holds player-independent rules, e.g., board size. */
final class Rules {

	final int boardWidth;

	final double komi;

	/** In seconds. If there is no time limit, this is a nonpositive number. */
	final int time;

	Rules(int boardSize, double komi, int time) {
		this.boardWidth = boardSize;
		this.komi = komi;
		this.time = time;
	}

}
