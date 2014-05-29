package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;

public final class NearAnotherStone extends AbstractFeature {

	public NearAnotherStone(Board board) {
		super(board);
	}
	
	@Override
	public boolean at(short p){
		return false;
	}

}
