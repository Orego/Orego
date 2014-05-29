package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;

public class OnThirdOrFourthLine implements Feature{
	
	public final Board board;
	
	public OnThirdOrFourthLine(Board board){
		this.board=board;
	}
	
	
	@Override
	public final boolean at(short p){
		return board.getCoordinateSystem().isOnThirdOrFourthLine(p);
	}

}
