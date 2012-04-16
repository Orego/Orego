package orego.util;

import static orego.core.Coordinates.pointToString;

/**
 * A set implementation that offers constant time insertion, search, deletion,
 * clearing, and size, assuming that the keys are all in the range [0, n-1].
 * If space is important, or if the set is fairly dense, BitVector may be
 * preferable.
 * @see BitVector
 */
public class IntSet {

	/** data[i] is the ith element of this set. */
	private int[] data;

	/** locations[i] is the index in data where i is stored (if any). */
	private int[] locations;

	/** Number of elements in this set. */
	private int size;

	/** All keys must be in [0, capacity). */
	public IntSet(int capacity) {
		data = new int[capacity];
		locations = new int[capacity];
	}

	/**
	 * Adds key, which may or may not be present, to this set.
	 */
	public void add(int key) {
		if (!contains(key)) {
			addKnownAbsent(key);
		}
	}

	/** Adds key, which is known to be absent, to this set. */
	public void addKnownAbsent(int key) {
		data[size] = key;
		locations[key] = size;
		size++;
	}

	/** Removes all elements from this set. */
	public void clear() {
		size = 0;
	}

	/** Returns true if key is in this set. */
	public boolean contains(int key) {
		int location = locations[key];
		return (location < size) & (data[locations[key]] == key);
	}

	/**
	 * Returns true if this is a (non-proper) subset of that. Note that this
	 * might return true even if the respective universes are different.
	 */
	public boolean isSubset(IntSet that) {
		for (int i = 0; i < size; i++) {
			if (!that.contains(get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Makes this into a copy of that, without the overhead of creating a new
	 * object.
	 */
	public void copyDataFrom(IntSet that) {
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
		IntSet that = (IntSet) obj;
		if (that.data.length != data.length) {
			// If we have different universes, we're not equal.
			return false;
		}
		return this.isSubset(that) && that.isSubset(this);
	}

	/** Returns the ith element of this list. */
	public int get(int i) {
		return data[i];
	}

	/** Returns true if this set is empty. */
	public boolean isEmpty() {
		return size == 0;
	}

	/** Removes key, which may or may not be present, from this set. */
	public void remove(int key) {
		if (contains(key)) {
			removeKnownPresent(key);
		}
	}

	/** Removes key, which is known to be present, from this set. */
	public void removeKnownPresent(int key) {
		size--;
		int location = locations[key];
		int replacement = data[size];
		data[location] = replacement;
		locations[replacement] = location;
	}

	/** Returns the number of elements in this set. */
	public int size() {
		return size;
	}

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
	public String toStringAsPoints() {
		String result = size + ": {";
		if (size > 0) {
			result += pointToString(data[0]);
			for (int i = 1; i < size; i++) {
				result += " " + pointToString(data[i]);
			}
		}
		return result + "}";
	}

	/** This set becomes the result of the union of this and other. */
	public void union(IntSet other) {
		for (int i = 0; i < other.size; i++) {
			add(other.get(i));
		}
	}

}
