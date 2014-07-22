package edu.lclark.orego.util;

import static java.util.Arrays.fill;

import java.io.Serializable;

/**
 * Equivalent to an array of booleans, but much smaller. Handy for representing
 * a set when insertion, deletion, clearing, and search are the only operations;
 * the universe is small; and space is of the essence. If it is necessary to
 * quickly compute the size of the set or traverse the elements, or if the set
 * is very sparse, ShortSet may be preferable.
 *
 * @see ShortSet
 */
@SuppressWarnings("serial")
public final class BitVector implements Serializable {

	/** The bits themselves, in 64-bit chunks. */
	private final long[] data;

	/** Elements must be in [0, capacity). */
	public BitVector(int capacity) {
		int longs = capacity / 64;
		if (longs * 64 < capacity) {
			longs++;
		}
		data = new long[longs];
	}

	/** Removes all elements from this set. */
	public void clear() {
		fill(data, 0L);
	}

	/** Returns true if i is in this set. */
	public boolean get(int i) {
		return (data[i / 64] & 1L << i % 64) != 0;
	}

	/** Sets whether i is in this set. */
	public void set(int i, boolean value) {
		if (value) {
			data[i / 64] |= 1L << i % 64;
		} else {
			data[i / 64] &= ~(1L << i % 64);
		}
	}

}
