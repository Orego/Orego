package orego.shape;

import orego.core.Colors;

public class DensePatternRepresentation {
	
	private int[] array;

	public DensePatternRepresentation(int[] array2) {
		array = new int[array2.length];
		// src, pos, dest, pos, length
		System.arraycopy(array2, 0, array, 0, array2.length);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DensePatternRepresentation))
			return false;
		int[] otherArray = ((DensePatternRepresentation) other).array;
		if (otherArray.length != array.length)
			return false;
		for (int i = 0; i < array.length; i++) {
			if (array[i] != otherArray[i])
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		int curInt = array[0];
		StringBuilder str = new StringBuilder("");
		for (int i = 0; i < array.length * 16; i+=2) {
			if (i % 16 == 0){
				curInt = array[i / 16];
			}
			if (i == array.length * 8) {
				str.append('.');
			}
			str.append(Colors.colorToChar((curInt >>> 14)&0x3));
			curInt <<= 2;
		}
		return str.toString();
	}
}
