package edu.lclark.orego.feature;

import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.*;

/**
 * True unless p is "like" an eye for the color to play, that is, is surrounded
 * by friendly stones and having no more than one (zero at the board edge)
 * diagonally adjacent enemy stones. It is almost always a bad idea to play in
 * such a point. The point p is assumed to be vacant.
 */
public final class NotEyeLike implements Predicate {

	private final Board board;
	
	public NotEyeLike(Board board) {
		this.board = board;
	}

	@Override
	public boolean at(short p) {
		assert board.getColorAt(p) == VACANT;
		StoneColor color = board.getColorToPlay();
		if(!board.hasMaxNeighborsForColor(color, p)){
			return true;
		}
		int count = 0;
		int oppositeIndex = color.opposite().index();
		short[] neighbors = board.getCoordinateSystem().getNeighbors(p);
		for(int i = FIRST_DIAGONAL_NEIGHBOR; i <= LAST_DIAGONAL_NEIGHBOR; i++){
			if(board.getColorAt(neighbors[i]).index() == oppositeIndex){
				count++;
				if(count == 2){
					return true;
				}
			}
		}
		
		if(board.getNeighborCount(color.opposite(), p) > 0 && count > 0){
			return true;
		}
		return false;
	}

}
