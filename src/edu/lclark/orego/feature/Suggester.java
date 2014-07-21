package edu.lclark.orego.feature;

import java.io.Serializable;

import edu.lclark.orego.util.ShortSet;

/**
 * Suggests moves having certain properties.
 */
public interface Suggester extends Serializable {

	/**
	 * Returns the suggested moves.
	 */
	public ShortSet getMoves();
	
	/** Returns the bias that this suggester should add to moves it suggests. */
	public int getBias();

}
