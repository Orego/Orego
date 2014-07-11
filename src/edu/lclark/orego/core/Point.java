package edu.lclark.orego.core;

import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.StoneColor.*;

import java.io.Serializable;

import edu.lclark.orego.util.*;

/**
 * Class to hold the information on a single point. This is separated out from
 * Board to make that class simpler. It may also make that class faster, as
 * individual Points (rather than larger arrays) can be swapped in and out of
 * cache. Because Point is only used by a class that fully understands it, its
 * fields are directly accessed.
 * 
 * Note that, elsewhere in Orego, Points are not passed around as arguments.
 * Primitive shorts are used instead.
 * 
 * @see edu.lclark.orego.core.Board
 * @see edu.lclark.orego.core.CoordinateSystem
 */
@SuppressWarnings("serial")
final class Point implements Serializable {

	/** Each field takes up this many bits. */
	static final int FIELD_SIZE = 3;

	/** A group of ones as wide as a field. */
	static final int MASK = (1 << FIELD_SIZE) - 1;

	/** Maximum number of neighbors a point can have. */
	static final int MAX_NEIGHBORS = 4;

	/** Number of bits by which each field is shifted. */
	static final int[] SHIFT = { 0 * FIELD_SIZE, 1 * FIELD_SIZE,
			2 * FIELD_SIZE };

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

	/**
	 * Stores the counts of the black, white, and vacant neighbors, using three
	 * bits of the int for each count.
	 */
	int neighborCounts;

	/**
	 * Add this to increment black and white neighbor counts and decrement
	 * vacant neighbor count.
	 */
	static final int EDGE_INCREMENT = (1 << SHIFT[BLACK.index()])
			+ (1 << SHIFT[WHITE.index()]) - (1 << SHIFT[VACANT.index()]);

	/** Counts for a point with four vacant neighbors. */
	public static final int FOUR_VACANT_NEIGHBORS = MAX_NEIGHBORS << SHIFT[VACANT
			.index()];

	/** Masks indicating the maximum number of neighbors in each color. */
	static final int[] MAX_COLOR_MASK = {
			(MAX_NEIGHBORS << SHIFT[BLACK.index()]),
			(MAX_NEIGHBORS << SHIFT[WHITE.index()]) };

	/**
	 * Add the BLACK or WHITE element to add one neighbor of that color and
	 * remove a vacant neighbor. Conversely, subtract to remove a stone and add
	 * a vacant neighbor.
	 */
	static final int[] NEIGHBOR_INCREMENT = {
			(1 << SHIFT[BLACK.index()]) - (1 << SHIFT[VACANT.index()]),
			(1 << SHIFT[WHITE.index()]) - (1 << SHIFT[VACANT.index()]) };

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
	 * @param directLiberties
	 *            The liberties directly around this point.
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
		neighborCounts = FOUR_VACANT_NEIGHBORS;
	}

	/** Copies data from that to this. */
	void copyDataFrom(Point that) {
		chainId = that.chainId;
		chainNextPoint = that.chainNextPoint;
		color = that.color;
		liberties.copyDataFrom(that.liberties);
		neighborCounts = that.neighborCounts;
	}

	/** Returns the number of neighbors of color c this point has. */
	public int getNeighborCount(Color c) {
		return (neighborCounts >> SHIFT[c.index()]) & MASK;
	}

	/**
	 * Returns true if this point has the maximum possible number of neighbors
	 * of color c.
	 */
	public boolean hasMaxNeighborsForColor(Color c) {
		return (neighborCounts & MAX_COLOR_MASK[c.index()]) == MAX_COLOR_MASK[c
				.index()];
	}

	/**
	 * Returns true if this point is in atari. Assumes that this point is the
	 * root of its chain.
	 */
	boolean isInAtari() {
		assert chainId == index;
		return liberties.size() == 1;
	}

}
