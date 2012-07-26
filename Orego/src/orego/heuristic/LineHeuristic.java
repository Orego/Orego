package orego.heuristic;

import orego.core.Board;
import orego.core.Coordinates;

public class LineHeuristic extends Heuristic {

	private static double[] values;
	
	public LineHeuristic(double weight) {
		setWeight(weight);
	}

	static {
		values = new double[Coordinates.EXTENDED_BOARD_AREA];
		if (Coordinates.BOARD_WIDTH == 9) {
			for (int i = 0; i < Coordinates.FIRST_POINT_BEYOND_BOARD; i++) {
				values[i] = Math
						.exp(-(((Coordinates.row(Coordinates.ALL_POINTS_ON_BOARD[i]) - 5)
								* (Coordinates.row(Coordinates.ALL_POINTS_ON_BOARD[i]) - 5) + (Coordinates
								.column(Coordinates.ALL_POINTS_ON_BOARD[i]) - 5) * (Coordinates.column(Coordinates.ALL_POINTS_ON_BOARD[i]) - 5))) / 4.5);
			}
		}
	}

	@Override
	public int evaluate(int p, Board board) {
		return (int) (500*values[p]);
	}

}
