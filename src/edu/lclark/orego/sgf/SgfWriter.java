package edu.lclark.orego.sgf;

import edu.lclark.orego.core.CoordinateSystem;

/** Contains methods for writing SGF files. */
public final class SgfWriter {

	/** Returns an SGF String representation of row or column i. */
	private static String rowOrColumnToStringSgf(int i) {
		return "" + "abcdefghijklmnopqrs".charAt(i);
	}

	public static String toSgf(short p, CoordinateSystem coords) {
		return rowOrColumnToStringSgf(coords.row(p))
				+ rowOrColumnToStringSgf(coords.column(p));
	}

}
