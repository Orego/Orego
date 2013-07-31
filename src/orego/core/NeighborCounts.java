package orego.core;

import static orego.core.Colors.*;

/**
 * This class contains a number of static fields and methods dealing with
 * neighbor counts. For efficiency, the number of black, white, and vacant
 * neighbors of each point are maintained. Off-board points are considered BOTH
 * black and white for these purposes, so a point at the edge of an empty board
 * has one black neighbor, one white neighbor, and three vacant neighbors.
 * <p>
 * The three neighbor counts for each point are stored within a single int. This
 * allows us to, for example, increment the black neighbor count and decrement
 * the vacant neighbor count with a single addition.
 * <p>
 * This implementation is based on Lukasz Lew's libEGO.
 */
public class NeighborCounts {

	/** Maximum number of neighbors a point can have. */
	protected static final int MAX_NEIGHBORS = 4;

	/** Each field takes up this many bits. */
	protected static final int FIELD_SIZE = 3;

	/** Number of bits by which each field is shifted. */
	protected static final int[] SHIFT = { 0 * FIELD_SIZE, 1 * FIELD_SIZE,
			2 * FIELD_SIZE };

	/** Counts for a point with four vacant neighbors. */
	public static final int FOUR_VACANT_NEIGHBORS = MAX_NEIGHBORS << SHIFT[VACANT];
	
	/**
	 * Add this to increment black and white neighbor counts and decrement
	 * vacant neighbor count.
	 */
	protected static final int EDGE_INCREMENT = (1 << SHIFT[BLACK])
			+ (1 << SHIFT[WHITE]) - (1 << SHIFT[VACANT]);

	/**
	 * Add the BLACK or WHITE element to add one neighbor of that color and
	 * remove a vacant neighbor. Conversely, subtract to remove a stone and add
	 * a vacant neighbor.
	 */
	protected static final int[] NEIGHBOR_INCREMENT = {
			(1 << SHIFT[BLACK]) - (1 << SHIFT[VACANT]),
			(1 << SHIFT[WHITE]) - (1 << SHIFT[VACANT]) };

	/** A group of ones as wide as a field. */
	protected static final int MASK = (1 << FIELD_SIZE) - 1;

	/** Masks indicating the maximum number of neighbors in each color. */
	protected static final int[] MAX_COLOR_MASK = {
			(MAX_NEIGHBORS << SHIFT[BLACK]), (MAX_NEIGHBORS << SHIFT[WHITE]) };

	/** Returns the number of neighbors of color indicated by counts. */
	public static int extractNeighborCount(int counts, int color) {
		return (counts >> SHIFT[color]) & MASK;
	}

	/**
	 * Returns true if counts indicates the maximum number of neighbors for
	 * color.
	 */
	public static boolean hasMaxNeighborsForColor(int counts, int color) {
		return (counts & MAX_COLOR_MASK[color]) == MAX_COLOR_MASK[color];
	}

}
