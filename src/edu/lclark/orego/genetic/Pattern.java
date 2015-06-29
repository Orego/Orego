package edu.lclark.orego.genetic;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.NonStoneColor;
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
	

	
	public boolean patternMatches(short p, Board board, int ... pattern){
		CoordinateSystem coords = board.getCoordinateSystem();
		int actualFriendly = 0;
		int actualEnemy = 0;
		int actualVacant = 0;
		int row = coords.row(p);
		int col = coords.column(p);
		int i = 0;
		for (int r = row -2; r <= row + 2; r++){
			for (int c = col - 2; c <= col + 2; c++){
				if ((c != col) || (r != row)){
					short temp = coords.at(r, c);
					final Color color = board.getColorAt(temp);
					if (color == board.getColorToPlay()) {
						actualFriendly |= (1 << i);
					} else if (color == board.getColorToPlay().opposite()){
						actualEnemy |= (1 << i);
					} else if (color == NonStoneColor.VACANT){
						actualVacant |= (1 << i);
					}
				}
				i++;
			}
		}
		
		
		System.out.println(Integer.toBinaryString(actualFriendly) + " " + Integer.toBinaryString(actualEnemy) + " " + Integer.toBinaryString(actualVacant));
		return ((((actualFriendly  << 6) & (pattern[0] << 6)) == (actualFriendly << 6)) 
				&& ((actualEnemy & pattern[1]) == actualEnemy) 
				&& ((actualVacant & pattern[2]) == actualVacant));
	}
}
