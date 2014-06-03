package edu.lclark.orego.feature;

import static java.lang.Math.min;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

/** True if p is on the third or fourth line. */
public final class OnThirdOrFourthLine implements Feature {
	
	private CoordinateSystem coords;
	
	public OnThirdOrFourthLine(Board board){
		coords = board.getCoordinateSystem();
	}

	@Override
	public boolean at(short p){
		int line = line(p);
		return (line >= 3) & (line <= 4);
	}

	/**
	 * Returns p's line (1-based) from the edge of the board
	 */
	private int line(short p) {
		int r = coords.row(p);
		r = min(r, coords.getWidth() - r - 1);
		int c = coords.column(p);
		c = min(c, coords.getWidth() - c - 1);
		return 1 + min(r, c);
	}

}
