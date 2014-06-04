package edu.lclark.orego.feature;

import edu.lclark.orego.util.ShortSet;

/**
 * Suggests moves according to certain rules
 */
public interface Suggester {

	/**
	 * Returns a ShortSet of moves that fulfill a certain criterion.
	 */
	public ShortSet get();
}
