package orego.heuristic;

import orego.core.Board;
import orego.core.Coordinates;

public class LineHeuristic extends Heuristic {

	private static double[] values;
	
	private static double[] morevalues;
	
	public LineHeuristic(int weight) {
		super(weight);
	}

	static {
		if (Coordinates.BOARD_WIDTH == 9) {
			values = new double[Coordinates.FIRST_POINT_BEYOND_BOARD];
			for (int i = 0; i < Coordinates.FIRST_POINT_BEYOND_BOARD; i++) {
				values[i] = Math
						.exp(-(((Coordinates.row(i) - 4)
								* (Coordinates.row(i) - 4) + (Coordinates
								.column(i) - 4) * (Coordinates.column(i) - 4))) / 4.5);
			}
		}
		if(Coordinates.BOARD_WIDTH == 19){
			morevalues = new double[Coordinates.FIRST_POINT_BEYOND_BOARD];
			values = new double[] {-2, 1,-1,-1,-1,-1,-1,-1,-1,-1,
									 1, 0, 0, 2, 2, 1, 1, 1, 1, 1,
									-1, 0, 10, 10, 10, 8, 6, 5, 4, 5,
									-1, 1, 10, 10, 8, 6, 4, 4, 5, 6,
									-1, 1, 10, 8, 5, 2, 2, 2, 2, 2, 
									-1, 1, 8, 6, 2, 2, 2, 2, 2, 2, 
									-1, 1, 6, 4, 2, 2, 2, 2, 2, 2,
									-1, 1, 5, 4, 2, 2, 2, 2, 2, 2,
									-1, 1, 4, 5, 2, 2, 2, 2, 2, 2,
									-1, 1, 5, 6, 2, 2, 2, 2, 2, 4};
			}
		int k = 0;
		for (int i = 0; i < Coordinates.BOARD_WIDTH; i++) {
			for (int j = 0; j <= 9; j++) {		
				if(i <= 9){
				morevalues[Coordinates.at(i,j)] = values[k];
				morevalues[Coordinates.at(i, 18-j)]= values[k];
				k++;
				}else{
					k--;
					morevalues[Coordinates.at(i,j)] = values[(18-i)*10 + j];
					morevalues[Coordinates.at(i, 18 -j)] = values[(18 - i) *10 + j];
				}
			}
		}
			
		}
	

	@Override
	public int evaluate(int p, Board board) {
		if(Coordinates.BOARD_WIDTH == 9){
			return (int) (10*values[p]);
		}else{
			return (int)(morevalues[p]);
		}

	}
}