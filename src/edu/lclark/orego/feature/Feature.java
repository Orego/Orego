package edu.lclark.orego.feature;

/** A feature detector. */
public interface Feature {

	/** Returns true if p has this feature. */
	public boolean at(short p);

}
