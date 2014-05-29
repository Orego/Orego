package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;

/** True if p is on the third or fourth line. */
public final class OnThirdOrFourthLine extends AbstractFeature {
	
	public OnThirdOrFourthLine(Board board){
		super(board);
	}

	@Override
	public boolean at(short p){
		// TODO Maybe pull this out of CoordinateSystem
		return getBoard().getCoordinateSystem().isOnThirdOrFourthLine(p);
	}

}
