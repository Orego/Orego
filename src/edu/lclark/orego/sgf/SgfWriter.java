package edu.lclark.orego.sgf;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import edu.lclark.orego.core.CoordinateSystem;

/** Contains methods for writing SGF files. */
public final class SgfWriter {

	/** Returns an SGF String representation of row or column i. */
	private static String rowOrColumnToStringSgf(int i) {
		return "" + "abcdefghijklmnopqrs".charAt(i);
	}

	/** Returns the SGF coordinates for p, including "" for pass. */
	public static String toSgf(short p, CoordinateSystem coords) {
		if (p == PASS) {
			return "";
		}
		return rowOrColumnToStringSgf(coords.row(p))
				+ rowOrColumnToStringSgf(coords.column(p));
	}

}
