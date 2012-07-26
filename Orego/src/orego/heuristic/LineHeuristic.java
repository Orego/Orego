package orego.heuristic;

import orego.core.Coordinates;

public class LineHeuristic {

	private static double[][] values;
	
	static {
		values = new double[Coordinates.BOARD_AREA][Coordinates.BOARD_AREA];
		if(Coordinates.BOARD_WIDTH == 9){
			for (int i = 0; i < Coordinates.BOARD_AREA; i++) {
				for (int j = 0; j < Coordinates.BOARD_AREA; j++) {					
					values[i][j] = Math.exp(-(((i-5)*(i-5)+(j-5)*(j-5))/4.5));
				}
			}
		}
		
	}
	
}
