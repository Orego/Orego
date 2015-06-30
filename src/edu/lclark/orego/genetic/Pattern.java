package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.Legality.OK;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.Legality;
import edu.lclark.orego.core.NonStoneColor;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;
import edu.lclark.orego.util.ShortSet;

public class Pattern {

	/*
	 * friendly = 100 4 enemy = 010 2 vacant = 001 1 friendly, enemy = 110 6 all
	 * three 111 7 enemy or vacant = 011 3 friendly or v = 101 5 off-board 000 0
	 */
	
	private ShortSet Candidates;
	
	private HistoryObserver historyObserver;
	
	private Board board;
	
	

	public Pattern(Board board, HistoryObserver historyObserver) {
		this.board = board;
		this.historyObserver = historyObserver;
		Candidates = board.getVacantPoints();
	}

	/**
	 * Given a row or column i and the width of the board, returns "actual",
	 * which will be actualFriendly or actualEnemy in the space patternMatcher
	 */
	private int edgePattern(int i, int width) {
		int actual = 0;
		if (i == 0) {
			actual |= (4 << 24);
		} else if (i == 1) {
			actual |= (2 << 24);
		} else if (i == 2) {
			actual |= (6 << 24);
		} else if (i == width - 3) {
			actual |= (1 << 24);
		} else if (i == width - 2) {
			actual |= (3 << 24);
		} else if (i == width - 1) {
			actual |= (5 << 24);
		}
		return actual;
	}

	public short patternMatcher(short p, int... pattern) {
		if (pattern[0] > 0) {
			if (spaceMatcher(p, pattern)){
				Legality legality = board.play(p);
				if (legality == OK) {
					return p;
				} else {
					Candidates.remove(p);
				}
			}
		} else {
			return timeMatcher(pattern);
			}
		return -1;
		}
	

	/**
	 * Given a position on the board (short) that is assumed to be empty, a
	 * board, and three ints containing the pattern, this returns a
	 * boolean--true if the pattern matches the condition of the position. Bit
	 * encoding for edge is as follows: for top and bottom edges, 3 bits (24-27
	 * in actualFriendly are equal to the row number. For left and right edges,
	 * 3 bits (24-27) in actualEnemy are equal to the column number.
	 */
	private boolean spaceMatcher(short p, int... pattern) {
		CoordinateSystem coords = board.getCoordinateSystem();
		int actualVacant = 0;
		int row = coords.row(p);
		int col = coords.column(p);
		int actualFriendly = edgePattern(row, coords.getWidth());
		int actualEnemy = edgePattern(col, coords.getWidth());
		int i = 0;
		for (int r = row - 2; r <= row + 2; r++) {
			for (int c = col - 2; c <= col + 2; c++) {
				if ((c != col) || (r != row)) {
					short temp = coords.at(r, c);
					final Color color = board.getColorAt(temp);
					if (color == board.getColorToPlay()) {
						actualFriendly |= (1 << i);
					} else if (color == board.getColorToPlay().opposite()) {
						actualEnemy |= (1 << i);
					} else if (color == NonStoneColor.VACANT) {
						actualVacant |= (1 << i);
					}
				}
				i++;
			}
		}
		// System.out.println(Integer.toBinaryString(actualFriendly) + " "
		// + Integer.toBinaryString(actualEnemy) + " "
		// + Integer.toBinaryString(actualVacant));
		return ((((actualFriendly) & (pattern[0])) == (actualFriendly))
				&& (((actualEnemy) & (pattern[1])) == (actualEnemy)) && ((actualVacant & pattern[2]) == actualVacant));
	}

	/**Returns true iff successful play was made.*/
	private short timeMatcher(int ... pattern) {
		int ultimate = 0;
		ultimate |= (historyObserver.get(board.getTurn() - 1));
		int penultimate = 0;
		if (pattern[1] > 0){
			penultimate |= (historyObserver.get(board.getTurn() - 2));
		}
		if (((ultimate & pattern[0]) == ultimate) && ((penultimate & pattern[1]) == penultimate)){
			Legality legality = board.play((short)pattern[2]);
			if (legality == OK){
				return (short)pattern[2];
			}
			Candidates.remove((short)pattern[2]);
		} 
		return -1;
	}

}
/*
 * Select and play one move:
 * 	Copy set of vacant points into candidates
 * 	for each rule
 * 		if time rule
 * 			p <-- matchTimeRule
 * 			if (p =/= OFF_BOARD OR p is an element of candidates)
 * 				if yes rule
 * 					try to play it
 * 				else 
 * 					remove it from candidates
 * 		if space rule
 * 			for each candidate (in random order)
 * 				if matchSpaceRule
 * 					if yes rule
 * 						try to play it
 * 					else
 * 						remove it
 * 		try all candidates
 * 		return pass
 */


