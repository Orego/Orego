package edu.lclark.orego.util;

import java.util.Arrays;

import edu.lclark.orego.core.CoordinateSystem;

/** Dense bit representation of a set of points on the board. */
public final class BitBoard {

	private final int[] bits;

	private final CoordinateSystem coords;

	private final int fullRow;
	
	public BitBoard(CoordinateSystem coords) {
		this.coords = coords;
		bits = new int[coords.getWidth()];
		fullRow = (1 << coords.getWidth()) - 1;
	}

	/** Turns off all of the bits. */
	public void clear() {
		for (int i = 0; i < bits.length; i++) {
			bits[i] = 0;
		}
	}

	/**
	 * Sets the bit for p.
	 */
	public void set(short p) {
		assert coords.isOnBoard(p);
		bits[coords.row(p)] |= (1 << coords.column(p));
	}

	/**
	 * Returns true if the bit for p is set.
	 */
	public boolean get(short p) {
		assert coords.isOnBoard(p);
		return (bits[coords.row(p)] & (1 << coords.column(p))) != 0;
	}

	public void expand() {
		int previous = 0;
		int i;
		for (i = 0; i < bits.length - 1; i++) {
			int temp = bits[i];
			bits[i] |= bits[i] << 1 | bits[i] >>> 1 | previous | bits[i + 1];
			previous = temp;
		}
		bits[i] |= bits[i] << 1 | bits[i] >>> 1 | previous;
	}

	public int getRow(int index) {
		return bits[index];
	}

	public void remove(short p) {
		assert coords.isOnBoard(p);
		bits[coords.row(p)] &= ~(1 << coords.column(p));
	}

	public void copyDataFrom(BitBoard source) {
		for (int i = 0; i < bits.length; i++) {
			bits[i] = source.getRow(i);
		}
	}

	@Override
	public String toString() {
		String result = "";
		for (int i = 0; i < coords.getWidth(); i++) {
			for (int j = 0; j < coords.getWidth(); j++) {
				result += (((bits[i] >> j) & 1) != 0) ? 'X' : '.';
			}
			result += "\n";
		}
		return result;
	}

	public boolean isFull() {
		for (int i = 0; i < bits.length; i++) {
			if (bits[i] != fullRow) {
				return false;
			}
		}
		return true;
	}

}
