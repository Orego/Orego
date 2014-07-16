package edu.lclark.orego.score;

import edu.lclark.orego.core.Board;

/**
 * Determines the score at the end of the game.
 *
 * @see PlayoutScorer
 */
public interface FinalScorer extends Scorer {

	double score(Board stonesRemoved);
	
}