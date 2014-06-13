package edu.lclark.orego.feature;

import java.io.Serializable;

/** Determines whether individual points on the board satisfy some predicate. */
public interface Predicate extends Serializable {

	/** Returns true if p satisfies this predicate. */
	public boolean at(short p);

}
