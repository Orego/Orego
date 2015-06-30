package edu.lclark.orego.genetic;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.NonStoneColor;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;

public class Pattern {

	/*
	 * friendly = 100 4 enemy = 010 2 vacant = 001 1 friendly, enemy = 110 6 all
	 * three 111 7 enemy or vacant = 011 3 friendly or v = 101 5 off-board 000 0
	 */

	/**
	 * Given a position on the board (short) that is assumed to be empty, a
	 * board, and three ints containing the pattern, this returns a
	 * boolean--true if the pattern matches the condition of the position. Bit
	 * encoding for edge is as follows: for top and bottom edges, 3 bits (24-27
	 * in actualFriendly are equal to the row number. For left and right edges,
	 * 3 bits (24-27) in actualEnemy are equal to the column number.
	 */
	public boolean patternMatcher(short p, Board board, HistoryObserver historyObserver, int... pattern) {
		CoordinateSystem coords = board.getCoordinateSystem();
		int actualFriendly = 0;
		int actualEnemy = 0;
		int actualVacant = 0;
		if (pattern[0] > 0) {
			int row = coords.row(p);
			int col = coords.column(p);
			if (row == 0) {
				actualFriendly |= (4 << 24);
			} else if (row == 1) {
				actualFriendly |= (2 << 24);
			} else if (row == 2) {
				actualFriendly |= (6 << 24);
			} else if (row == coords.getWidth() - 3) {
				actualFriendly |= (1 << 24);
			} else if (row == coords.getWidth() - 2) {
				actualFriendly |= (3 << 24);
			} else if (row == coords.getWidth() - 1) {
				actualFriendly |= (5 << 24);
			}
			if (col == 0) {
				actualEnemy |= (4 << 24);
			} else if (col == 1) {
				actualEnemy |= (2 << 24);
			} else if (col == 2) {
				actualEnemy |= (6 << 24);
			} else if (col == coords.getWidth() - 3) {
				actualEnemy |= (1 << 24);
			} else if (col == coords.getWidth() - 2) {
				actualEnemy |= (3 << 24);
			} else if (col == coords.getWidth() - 1) {
				actualEnemy |= (5 << 24);
			}

//			actualFriendly |= (row << 24);
//			actualEnemy |= (col << 24);
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
		} else {
			 
			
		}
		// System.out.println(Integer.toBinaryString(actualFriendly) + " "
		// + Integer.toBinaryString(actualEnemy) + " "
		// + Integer.toBinaryString(actualVacant));
		return ((((actualFriendly) & (pattern[0])) == (actualFriendly))
				&& (((actualEnemy) & (pattern[1])) == (actualEnemy)) && ((actualVacant & pattern[2]) == actualVacant));
	}
}
