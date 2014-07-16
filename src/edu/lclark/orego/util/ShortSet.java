package edu.lclark.orego.util;

import java.io.Serializable;

import edu.lclark.orego.core.CoordinateSystem;

/**
 * A set implementation that offers constant time insertion, search, deletion,
 * clearing, and size, assuming that the keys are all in the range [0, n). If
 * space is important, or if the set is fairly dense, BitVector may be
 * preferable.
 */
@SuppressWarnings("serial")
public final class ShortSet implements Serializable {

	/** data[i] is the ith element of this set. */
	private final short[] data;

	/** locations[i] is the index in data where i is stored (if any). */
	private final short[] locations;

	/** Number of elements in this set. */
	private int size;

	/** All keys must be in [0, capacity). */
	public ShortSet(int capacity) {
		data = new short[capacity];
		locations = new short[capacity];
	}

	/**
	 * Adds key, which may or may not be present, to this set.
	 */
	public void add(short key) {
		if (!contains(key)) {
			addKnownAbsent(key);
		}
	}

	/** This set becomes the result of the union of this and that. */
	public void addAll(ShortSet that) {
		for (short i = 0; i < that.size; i++) {
			add(that.get(i));
		}
	}

	/**
	 * Adds key, which is known to be absent, to this set. This is faster than
	 * add.
	 */
	public void addKnownAbsent(short key) {
		data[size] = key;
		locations[key] = (short) size;
		size++;
	}

	/** Removes all elements from this set. */
	public void clear() {
		size = 0;
	}

	/** Returns true if key is in this set. */
	public boolean contains(short key) {
		final int location = locations[key];
		return location < size & data[locations[key]] == key;
	}

	/**
	 * Makes this into a copy of that, without the overhead of creating a new
	 * object.
	 */
	public void copyDataFrom(ShortSet that) {
		size = that.size;
		System.arraycopy(that.data, 0, data, 0, size);
		System.arraycopy(that.locations, 0, locations, 0, locations.length);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ShortSet that = (ShortSet) obj;
		if (that.data.length != data.length) {
			// If we have different universes, we're not equal.
			return false;
		}
		if (that.size != size) {
			return false;
		}
		for (int i = 0; i < size; i++) {
			if (!that.contains(data[i])) {
				return false;
			}
		}
		return true;
	}

	/** Returns the ith element of this list. */
	public short get(int i) {
		return data[i];
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException(
				"ShortSets are not suitable for storing in hash tables");
	}

	/** Removes key, which may or may not be present, from this set. */
	public void remove(short key) {
		if (contains(key)) {
			removeKnownPresent(key);
		}
	}

	/**
	 * Removes key, which is known to be present, from this set. This is faster
	 * than remove.
	 */
	public void removeKnownPresent(int key) {
		size--;
		final short location = locations[key];
		final short replacement = data[size];
		data[location] = replacement;
		locations[replacement] = location;
	}

	/** Returns the number of elements in this set. */
	public int size() {
		return size;
	}

	@Override
	public String toString() {
		String result = "{";
		if (size > 0) {
			result += data[0];
			for (int i = 1; i < size; i++) {
				result += ", " + data[i];
			}
		}
		return result + "}";
	}

	/**
	 * Similar to toString(), but displays human-readable point labels (e.g.,
	 * "d3") instead of ints.
	 */
	public String toString(CoordinateSystem coords) {
		String result = size + ": {";
		if (size > 0) {
			result += coords.toString(data[0]);
			for (int i = 1; i < size; i++) {
				result += ", " + coords.toString(data[i]);
			}
		}
		return result + "}";
	}

}
