package edu.lclark.orego.feature;

/** Determines whether individual points on the board satisfy some predicate. */
public interface Predicate {

	/** Returns true if p satisfies this predicate. */
	public boolean at(short p);

}
