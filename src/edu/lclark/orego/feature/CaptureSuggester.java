package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.util.ShortSet;

public class CaptureSuggester implements Suggester{
	
	private final Board board;
	
	
	/**
	 * A list of all of the moves for the current player to play that will capture stones
	 */
	private final ShortSet movesToCapture;
	
	public ShortSet get(){
		movesToCapture.clear();
		ShortSet enemyChains = board.getChains(board.getColorToPlay().opposite());
		for(int i = 0; i < enemyChains.size(); i++){
			ShortSet liberties = board.getLiberties(enemyChains.get(i));
			if(liberties.size()==1){
				movesToCapture.add(liberties.get(0));
			}
		}
		return movesToCapture;
	}
	
	public CaptureSuggester(Board board){
		this.board = board;
		movesToCapture = new ShortSet(board.getCoordinateSystem().getFirstPointBeyondBoard());
	}

}
