package edu.lclark.orego.util;

import java.util.Arrays;

import edu.lclark.orego.core.CoordinateSystem;

/** Dense bit representation of a set of points on the board. */
public final class BitBoard {

	private int[] bits;
	
	private int[] expansion;

	private final CoordinateSystem coords;
	
	public BitBoard(CoordinateSystem coords) {
		this.coords = coords;
		bits = new int[coords.getWidth()];
		expansion = new int[coords.getWidth()];
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
		Arrays.fill(expansion, 0);
		for(int i = 0; i< bits.length; i++){
			expansion[i] |= bits[i];
			if(i!=0){
				expansion[i] |= bits[i-1];
			}
			if(i<coords.getWidth()-1){
				expansion[i] |= bits[i+1];
			}
			expansion[i] |= bits[i] >> 1;
			expansion[i] |= bits[i] << 1;
		}
		int[] temp = bits;
		bits=expansion;
		expansion = temp;
	}
	
	public String toString() {
		String result = "";
		for(int i = 0; i<coords.getWidth(); i++){
			for(int j = 0; j<coords.getWidth(); j++){
				result += (bits[i] >> j) & 1;
			}
			result += "\n";
		}
		return result;
	}

}
