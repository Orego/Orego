package edu.lclark.orego.feature;

import static java.lang.Math.min;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

/** True if p is on the third or fourth line. */
public final class OnThirdOrFourthLine extends AbstractFeature {
	
	public OnThirdOrFourthLine(Board board){
		super(board);
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
		CoordinateSystem coords = getBoard().getCoordinateSystem();
		int r = coords.row(p);
		r = min(r, coords.getWidth() - r - 1);
		int c = coords.column(p);
		c = min(c, coords.getWidth() - c - 1);
		return 1 + min(r, c);
	}

}
