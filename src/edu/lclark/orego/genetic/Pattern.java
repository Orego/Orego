package edu.lclark.orego.genetic;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;

public class Pattern {
	
	/**
	 *  friendly = 			100		4
	 *  enemy = 			010  	2
	 *  vacant = 			001 	1
	 *  
	 *  
	 *  friendly, enemy = 	110 	6
	 *  all three 			111 	7
	 *  enemy or vacant = 	011   	3
	 *  friendly or v	= 	101		5
	 *  off-board			000		0
	 */
	

	
	boolean patternMatches(short p, Board board, int ... pattern){
		CoordinateSystem coords = board.getCoordinateSystem();
		boolean matches = true;
		int row = coords.row(p);
		int col = coords.column(p);
		for (int r = row -2; r < row + 2; r++){
			for (int c = col - 2; c < col + 2; c++){
				if ((c != col) || (r != row)){
					short temp = coords.at(r, c);
					final Color color = board.getColorAt(temp);
					if (color == board.getColorToPlay()) {
						// Friendly stone at this neighbor
					}
				}
			}
		}
		return matches;
	}

}
