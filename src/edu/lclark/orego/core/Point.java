package edu.lclark.orego.core;

import edu.lclark.orego.util.*;

/**
 * Class to hold the information on a single point. This is separated out from
 * BoardImplementation to make that class simpler. It may also make that class
 * faster, as individual Points (rather than larger arrays) can be swapped in
 * and out of cache. Because Point is only used by a class that fully understand
 * it, its fields are directly accessed.
 * 
 * Note that Points are not passed around as arguments. Primitive shorts are
 * used instead.
 * 
 * @see edu.lclark.orego.core.CoordinateSystem.
 */
final class Point {

	/** Liberties of this point if it is the root of a chain. */
	final ShortSet liberties;
	
	/** Color of this point. */
	Color color;
	
	/**
	 * Identifier of chain for this point (location of the "root" stone in that
	 * chain). The chainId of a vacant point is the point's own location.
	 */
	short chainId;
	
	/**
	 * Next "pointers" for this point, linking points into chains.
	 */
	short chainNextPoint;
	
	Point(CoordinateSystem coords) {
		short n = coords.getFirstPointBeyondBoard();
		liberties = new ShortSet(n);
	}

}
