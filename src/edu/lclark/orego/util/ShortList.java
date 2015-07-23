package edu.lclark.orego.util;

import java.io.Serializable;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;

/**
 * Similar to java.util.ArrayList<Short>, but avoids various overhead such as
 * wrappers. This class is much less safe than ArrayList; for example, you can
 * set a key beyond the size of the array (although an assertion checks this).
 * This is in the name of speed.
 * <p>
 * The addIfNotPresent() method allows a ShortList to function as a set. If the
 * keys come from a small, finite set, ShortSet or BitVector may be more
 * efficient.
 */
@SuppressWarnings("serial")
public final class ShortList implements Serializable {

	/** List elements. */
	private final short[] data;

	/** Number of elements in list; also index of next available space. */
	private int size;

	public ShortList(int capacity) {
		data = new short[capacity];
	}

	/**
	 * Adds key to the end of this list. If it is already present, a second copy
	 * is added.
	 */
	public void add(short key) {
		data[size] = key;
		size++;
	}

	/** Adds key to this list if it is not already present. */
	public void addIfNotPresent(short key) {
		if (!contains(key)) {
			add(key);
		}
	}

	/** Returns the number of elements this list can hold. */
	public int capacity() {
		return data.length;
	}

	/** Removes all elements from this list. */
	public void clear() {
		size = 0;
	}

	/** Returns true if this list contains key. */
	public boolean contains(short key) {
		for (int i = 0; i < size; i++) {
			if (data[i] == key) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Makes this into a copy of that, without the overhead of creating a new
	 * object.
	 */
	public void copyDataFrom(ShortList that) {
		size = that.size;
		System.arraycopy(that.data, 0, data, 0, size);
	}

	/** Returns the ith element of this list. */
	public short get(int i) {
		assert i < size;
		return data[i];
	}

	/**
	 * Removes and returns the last element of this list.
	 *
	 * @throws ArrayIndexOutOfBoundsException
	 *             if this list is empty.
	 */
	public short removeLast() {
		size--;
		return data[size];
	}

	/**
	 * Removes and returns a random element of this list. The order of this list
	 * is not maintained.
	 */
	public short removeRandom(MersenneTwisterFast random) {
		int randomIndex = random.nextInt(size);
		short temp = data[randomIndex];
		size--;
		data[randomIndex] = data[size];
		return temp;
	}

	/** Sets the ith element of this list. */
	public void set(int i, short key) {
		assert i < size;
		data[i] = key;
	}

	/** Returns the size of this list. */
	public int size() {
		return size;
	}

	@Override
	public String toString() {
		String result = "";
		if (size > 0) {
			result += data[0];
			for (int i = 1; i < size; i++) {
				result += ", " + data[i];
			}
		}
		return "(" + result.trim() + ")";
	}

	/**
	 * Returns a String with the elements of this list represented as
	 * human-readable board coordinates.
	 */
	public String toString(CoordinateSystem coords) {
		String result = "";
		if (size > 0) {
			result += coords.toString(data[0]);
			for (int i = 1; i < size; i++) {
				result += ", " + coords.toString(data[i]);
			}
		}
		return "(" + result + ")";
	}

	/** Adds all of the elements in vacantPoints to this list. */
	public void addAll(ShortSet set) {
		for (int i = 0; i < set.size(); i++) {
			data[i] = set.get(i);
		}
		size = set.size();
	}

}
