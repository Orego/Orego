package edu.lclark.orego.score;

import java.io.Serializable;
import edu.lclark.orego.core.Color;

// TODO Create subinterfaces FinalScorer and PlayoutScorer
public interface Scorer extends Serializable {
	
	/** Returns the komi used by this scorer. */
	public double getKomi();
	
	/**
	 * Returns the difference between the scores of the two players at the current board state.
	 * A positive number is good for black.
	 */
	public double score();
	
	
	/**
	 * Returns the winner at a current board state. This can be vacant in the case of a tie.
	 */
	public Color winner();

}
