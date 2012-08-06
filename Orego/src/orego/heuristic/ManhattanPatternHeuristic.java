package orego.heuristic;


import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.*;
import orego.core.Board;

public class ManhattanPatternHeuristic extends AbstractPatternHeuristic {
	
	private int[][] region;

	public ManhattanPatternHeuristic(int weight){
		super(weight);
		region = MANHATTAN_NEIGHBORHOOD[3];
	}
	

	@Override
	public void setProperty(String property, String value) {
		super.setProperty(property, value);
		if (property.equals("radius")) {
			region = MANHATTAN_NEIGHBORHOOD[Integer.valueOf(value)];
		}
	}

	@Override
	public void prepare(Board board) {
		super.prepare(board);
		for (int p : region[board.getMove(board.getTurn()-1)]) {
			if (board.getColor(p) == VACANT) {
				char neighborhood = board.getNeighborhood(p);
				if(GOOD_NEIGHBORHOODS[board.getColorToPlay()].get(neighborhood)) {
					recommend(p);
				}
				if(BAD_NEIGHBORHOODS[board.getColorToPlay()].get(neighborhood)) {
					discourage(p); 
				}
			}
		}
	}

}
