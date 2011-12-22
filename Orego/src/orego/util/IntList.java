package orego.util;

import static orego.core.Coordinates.*;

/**
 * Similar to java.util.ArrayList<Integer>, but avoids various overhead such as
 * wrappers. This class is much less safe than ArrayList; for example, you can
 * set a key beyond the size of the array. This is in the name of speed.
 * <p>
 * The addIfNotPresent() method allows an IntList to function as a set. If the
 * keys come from a small, finite set, IntSet or BitVector may be more
 * efficient.
 */
public class IntList {

	/** List elements. */
	private int[] data;

	/** Number of elements in list; also index of next available space. */
	private int size;

	public IntList(int capacity) {
		data = new int[capacity];
	}

	/** Adds key to the end of this list. */
	public void add(int key) {
		data[size] = key;
		size++;
	}

	/** Adds key to this list if it is not already present. */
	public void addIfNotPresent(int key) {
		if (!contains(key)) {
			add(key);
		}
	}

	/** Returns the number of elements this list can hold. */
	public int capacity() {
		return data.length;
	}

	/** Sets the size of this list to 0. */
	public void clear() {
		size = 0;
	}

	/** Returns true if this list contains key. */
	public boolean contains(int key) {
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
	public void copyDataFrom(IntList that) {
		size = that.size();
		System.arraycopy(that.data, 0, data, 0, size);
	}

	/** Returns the ith element of this list. */
	public int get(int i) {
		assert i < size;
		return data[i];
	}

	/**
	 * Removes the element at index i and returns the last element, which is
	 * moved to replace the deleted element.
	 */
	public int removeAt(int i) {
		size--;
		data[i] = data[size];
		return data[i];
	}

	/** Removes and returns the last element of this list. */
	public int removeLast() {
		size--;
		return data[size];
	}

	/** Sets the ith element of this list. */
	public void set(int i, int key) {
		assert i < size;
		data[i] = key;
	}

	/** Returns the size of this list. */
	public int size() {
		return size;
	}

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
	public String toStringAsPoints() {
		String result = "";
		if (size > 0) {
			result += pointToString(data[0]);
			for (int i = 1; i < size; i++) {
				result += " " + pointToString(data[i]);
			}
		}
		return "(" + result + ")";
	}

}
