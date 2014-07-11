package edu.lclark.orego.core;

import java.io.Serializable;

/**
 * Set of Zobrist hashes (longs) from previous board positions. This is a hash
 * table, but without all of the overhead of java.util.HashSet. It only supports
 * insertion, search, and copying. Collisions are resolved by linear probing.
 * The special value EMPTY is always considered to be in the table.
 */
@SuppressWarnings("serial")
public final class SuperKoTable implements Serializable {

	/**
	 * Special value for an empty slot in the table. This number also represents
	 * an empty board, so it is always considered to be in the table.
	 */
	public static final long EMPTY = 0L;

	/**
	 * Bit mask to make hash codes positive. Math.abs() won't work because
	 * abs(Integer.minValue()) < 0.
	 */
	public static final int IGNORE_SIGN_BIT = 0x7fffffff;

	/** The table proper. */
	private final long[] data;

	public SuperKoTable(CoordinateSystem coords) {
		data = new long[coords.getMaxMovesPerGame() * 2];
	}

	/** Adds key to this table. */
	public void add(long key) {
		if (key != EMPTY) {
			int slot = ((int) key & IGNORE_SIGN_BIT) % data.length;
			while (data[slot] != EMPTY) {
				if (data[slot] == key) {
					return;
				}
				slot = (slot + 1) % data.length;
			}
			data[slot] = key;
		}
	}

	/** Returns the number of slots in the table. */
	int capacity() {
		return data.length;
	}

	/** Removes all elements from this table. */
	public void clear() {
		java.util.Arrays.fill(data, EMPTY);
	}

	/** Returns true if key is in this table. */
	public boolean contains(long key) {
		if (key == EMPTY) {
			return true;
		}
		int slot = ((int) key & IGNORE_SIGN_BIT) % data.length;
		while (data[slot] != EMPTY) {
			if (data[slot] == key) {
				return true;
			}
			slot = (slot + 1) % data.length;
		}
		return false;
	}

	/**
	 * Makes this into a copy of that, without the overhead of creating a new
	 * object.
	 */
	public void copyDataFrom(SuperKoTable that) {
		System.arraycopy(that.data, 0, data, 0, data.length);
	}

}
