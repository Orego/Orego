package edu.lclark.orego.move;

import java.io.Serializable;

import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/** Plays moves. */
public interface Mover extends Serializable {

	/**
	 * Selects and plays one move.
	 *
	 * @param fast If true, uses playFast instead of play.
	 */
	public short selectAndPlayOneMove(MersenneTwisterFast random, boolean fast);

}