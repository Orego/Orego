package edu.lclark.orego.book;

import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.SuperKoTable.IGNORE_SIGN_BIT;

import java.io.Serializable;

/**
 * Maps longs to shorts.
 *
 * The underlying data structure is a hash table with linear probing.
 *
 * This mimics java.util.HashMap<Long, Short>, but it does not support removal
 * and is significantly more space-efficient.
 */
public final class SmallHashMap implements Serializable {

	/** For serialization. */
	private static final long serialVersionUID = 1L;

	/** Keys. */
	private long[] keys;

	/** How many keys are currently in the map. */
	private int size;

	/** Values. */
	private short[] values;

	public SmallHashMap() {
		keys = new long[1];
		values = new short[1];
		values[0] = NO_POINT;
	}

	/** Returns true if this map contains key. */
	public boolean containsKey(long key) {
		int slot = ((int) key & IGNORE_SIGN_BIT) % keys.length;
		while (true) {
			if (keys[slot] == key && values[slot] != NO_POINT) {
				return true;
			} else if (values[slot] == NO_POINT) {
				return false;
			}
			slot = (slot + 1) % keys.length;
		}
	}

	/** Returns the value associated with key. */
	public short get(long key) {
		int slot = ((int) key & IGNORE_SIGN_BIT) % keys.length;
		while (true) {
			if (keys[slot] == key && values[slot] != NO_POINT) {
				return values[slot];
			} else if (values[slot] == NO_POINT) {
				return NO_POINT;
			}
			slot = (slot + 1) % keys.length;
		}
	}

	/** Returns the raw key array. */
	long[] getKeys() {
		return keys;
	}

	/** Associates key with value, stretching the map if it is too full. */
	public void put(long key, short value) {
		assert value != NO_POINT;
		size++;
		// The maximum load factor is 0.5
		if (size > keys.length / 2) {
			rehash();
		}
		putAfterTableKnownLargeEnough(key, value);
	}

	/**
	 * Associates key with value. Does not check that this map is large enough.
	 */
	private void putAfterTableKnownLargeEnough(long key, short value) {
		int slot = ((int) key & IGNORE_SIGN_BIT) % keys.length;
		while (true) {
			// If this map already has the value, it doesn't do anything.
			if (keys[slot] == key && values[slot] != NO_POINT) {
				return;
			} else if (values[slot] == NO_POINT) {
				keys[slot] = key;
				values[slot] = value;
				return;
			}
			slot = (slot + 1) % keys.length;
		}
	}

	/**
	 * Copies the data into tables twice as large, stretching the map.
	 */
	private void rehash() {
		final long[] oldKeys = keys;
		final short[] oldValues = values;
		keys = new long[keys.length * 2];
		values = new short[values.length * 2];
		for (int i = 0; i < values.length; i++) {
			values[i] = NO_POINT;
		}
		for (int i = 0; i < oldKeys.length; i++) {
			if (oldValues[i] != NO_POINT) {
				putAfterTableKnownLargeEnough(oldKeys[i], oldValues[i]);
			}
		}
	}

}
