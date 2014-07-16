package edu.lclark.orego.core;

/** See subclasses for the available colors. */
public interface Color {

	/** Returns the char, used in diagrams, for this color. */
	public char toChar();

	/**
	 * Returns a number that can be used index into an array. It should be
	 * unique for each color value.
	 */
	public int index();

}
