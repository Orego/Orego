package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;

public class NearAnotherStone implements Feature{

	
	private final Board board;

	public NearAnotherStone(Board board) {
		this.board = board;
	}
	
	@Override
	public final boolean at(short p){
		
		
		return false;
	}
	
}
