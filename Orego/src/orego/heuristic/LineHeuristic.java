package orego.heuristic;

import orego.core.Board;
import orego.core.Coordinates;
import static orego.core.Coordinates.*;
import static java.lang.Math.*;

public class LineHeuristic extends Heuristic {

	/** Values for one corner of the 19x19 board. */
	private static final int[] CORNER =
		{-2, 1,-1,-1,-1,-1,-1,-1,-1,-1,
		 1, 0, 0, 2, 2, 1, 1, 1, 1, 1,
		-1, 0, 10, 10, 10, 8, 6, 5, 4, 5,
		-1, 1, 10, 10, 8, 6, 4, 4, 5, 6,
		-1, 1, 10, 8, 5, 2, 2, 2, 2, 2, 
		-1, 1, 8, 6, 2, 2, 2, 2, 2, 2, 
		-1, 1, 6, 4, 2, 2, 2, 2, 2, 2,
		-1, 1, 5, 4, 2, 2, 2, 2, 2, 2,
		-1, 1, 4, 5, 2, 2, 2, 2, 2, 2,
		-1, 1, 5, 6, 2, 2, 2, 2, 2, 4};
	
	/** values[p] is the value of playing at point p. */
	private static final int[] VALUES = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
	
	static {
		assert ((BOARD_WIDTH == 9) || (BOARD_WIDTH == 19)) : "Invalid board size for LineHeuristic";
		if (BOARD_WIDTH == 9) {
			for (int i = 0; i < FIRST_POINT_BEYOND_BOARD; i++) {
				VALUES[i] = (int) (10 * exp(-(((row(i) - 4) * (row(i) - 4) + (column(i) - 4)
						* (column(i) - 4))) / 4.5));
			}
		}
		if (BOARD_WIDTH == 19) {
			int k = 0;
			for (int i = 0; i < BOARD_WIDTH; i++) {
				for (int j = 0; j <= 9; j++) {
					if (i <= 9) {
						VALUES[at(i, j)] = CORNER[k];
						VALUES[at(i, 18 - j)] = CORNER[k];
						k++;
					} else {
						k--;
						VALUES[at(i, j)] = CORNER[(18 - i) * 10 + j];
						VALUES[at(i, 18 - j)] = CORNER[(18 - i) * 10 + j];
					}
				}
			}
		}
	}

	public LineHeuristic(int weight) {
		super(weight);
	}
	
	@Override
	public int evaluate(int p, Board board) {
		return VALUES[p];
	}

}