package edu.lclark.orego.feature;

/** Detects features for individual points on the board. */
public interface Feature {

	/** Returns true if p has this feature. */
	public boolean at(short p);

}
