package edu.lclark.orego.feature;

import edu.lclark.orego.util.ShortSet;

/**
 * Suggests moves having certain properties.
 */
public interface Suggester {

	/**
	 * Returns the suggested moves.
	 */
	public ShortSet get();

}
