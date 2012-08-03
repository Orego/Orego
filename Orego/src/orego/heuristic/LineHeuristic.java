package orego.heuristic;

import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.core.Coordinates;
import orego.util.IntSet;
import static orego.core.Coordinates.*;
import static java.lang.Math.*;

public class LineHeuristic extends Heuristic {

	/** Values for one corner of the 19x19 board. */
	private static final int[] CORNER = //
	{ -3, -1, -2, -2, -2, -2, -2, -2, -2, -2, //
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,//
			-2, -1, 0, 0, 0, 0, 0, 0, 0, 0, //
			-2, -1, 0, 0, 0, 0, 0, 0, 0, 0, //
			-2, -1, 0, 0, -1, -1, -1, -1, -1, -1, //
			-2, -1, 0, 0, -1, -1, -1, -1, -1, -1, //
			-2, -1, 0, 0, -1, -1, -1, -1, -1, -1,//
			-2, -1, 0, 0, -1, -1, -1, -1, -1, -1,//
			-2, -1, 0, 0, -1, -1, -1, -1, -1, -1,//
			-2, -1, 0, 0, -1, -1, -1, -1, -1, 0 };//

	/** values[p] is the value of playing at point p. */
	private static final int[] VALUES = new int[Coordinates.FIRST_POINT_BEYOND_BOARD];
	private static final IntSet NONZERO_POINTS = new IntSet(Coordinates.FIRST_POINT_BEYOND_BOARD);

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
						if(CORNER[k] != 0){
							NONZERO_POINTS.add(at(i, j));
							NONZERO_POINTS.add(at(i, 18 - j));
						}
						k++;
					} else {
						k--;
						VALUES[at(i, j)] = CORNER[(18 - i) * 10 + j];
						VALUES[at(i, 18 - j)] = CORNER[(18 - i) * 10 + j];
						if(CORNER[(18 - i) * 10 + j] != 0){
							NONZERO_POINTS.add(at(i, j));
							NONZERO_POINTS.add(at(i, 18 - j));
						}
					}
				}
			}
		}
	}

	public LineHeuristic(int weight) {
		super(weight);
		setValues(VALUES);
		setNonzeroPoints(NONZERO_POINTS);
		setBestMove(NO_POINT);
	}

	@Override
	public void prepare(Board board, MersenneTwisterFast random) {
		// does nothing
	}

}