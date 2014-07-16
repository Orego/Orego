package edu.lclark.orego.score;

/**
 * Determines the score at the end of a playout. Assumes that everything on the
 * board is alive and that no territory is larger than one point in size.
 *
 * @see PlayoutScorer
 */
public interface PlayoutScorer extends Scorer {
	// No new methods
}
