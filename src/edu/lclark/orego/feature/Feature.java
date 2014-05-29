package edu.lclark.orego.feature;

/** A feature detector. */
public interface Feature {

	/** Returns true if p has this feature. */
	public boolean at(short p);
	
	/** Resets any data structures maintained by this feature. */
	public void clear();

	// TODO We'll need copyDataFrom from multithreading

}
