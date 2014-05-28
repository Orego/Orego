package edu.lclark.orego.core;

import static edu.lclark.orego.core.NonStoneColor.*;
import edu.lclark.orego.util.*;

/**
 * Class to hold the information on a single point. This is separated out from
 * BoardImplementation to make that class simpler. It may also make that class
 * faster, as individual Points (rather than larger arrays) can be swapped in
 * and out of cache. Because Point is only used by a class that fully understand
 * it, its fields are directly accessed.
 * 
 * Note that, elsewhere in Orego, Points are not passed around as arguments.
 * Primitive shorts are used instead.
 * 
 * @see edu.lclark.orego.core.BoardImplementation
 * @see edu.lclark.orego.core.CoordinateSystem.
 */
final class Point {

	/**
	 * Identifier of chain for this point (location of the "root" stone in that
	 * chain). The chainId of a vacant point is the point's own location.
	 */
	short chainId;

	/**
	 * Next "pointers" for this point, linking points into chains.
	 */
	short chainNextPoint;

	/** Color of this point. */
	Color color;

	/** Index of this point. */
	final short index;

	/** Liberties of this point if it is the root of a chain. */
	final ShortSet liberties;

	Point(CoordinateSystem coords, short index) {
		this.index = index;
		if (coords.isOnBoard(index)) {
			short n = coords.getFirstPointBeyondBoard();
			liberties = new ShortSet(n);
		} else {
			liberties = null;
			color = OFF_BOARD;
		}
	}

	/** Adds this stone to chain. */
	void addToChain(Point chain) {
		chainNextPoint = chain.chainNextPoint;
		chain.chainNextPoint = index;
		chainId = chain.index;
	}

	/**
	 * Makes this stone a single-stone chain.
	 * 
	 * @param directLiberties The liberties directly around this point.
	 */
	void becomeOneStoneChain(ShortSet directLiberties) {
		chainId = index;
		chainNextPoint = index;
		liberties.copyDataFrom(directLiberties);
	}

	/**
	 * Returns this point to its initial state. Should only be called on
	 * on-board points.
	 */
	void clear() {
		liberties.clear();
		color = VACANT;
		chainId = index;
	}

	/**
	 * Returns true if this point is in atari. Assumes that this point is the root of its chain.
	 */
	boolean isInAtari() {
		assert chainId == index;
		return liberties.size() == 1;
	}

}
