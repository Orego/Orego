package orego.heuristic;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.MANHATTAN_NEIGHBORHOOD;
import static orego.core.Coordinates.ON_BOARD;
import orego.core.Board;
import orego.play.UnknownPropertyException;

public abstract class AbstractManhattanPatternHeuristic extends
		AbstractPatternHeuristic {

	private int[][] region; 
	
	public AbstractManhattanPatternHeuristic(int weight) {
		super(weight);
	}
	
	protected void setRegion(int[][]  region) {
		this.region = region;
	}

	@Override
	public void setProperty(String property, String value) throws UnknownPropertyException {
		super.setProperty(property, value);
		if (property.equals("radius")) {
			region = MANHATTAN_NEIGHBORHOOD[Integer.valueOf(value)];
		}
	}

	@Override
	public void prepare(Board board) {
		super.prepare(board);
		int lastMove = board.getMove(board.getTurn() - 1);
		if (!ON_BOARD[lastMove]) {
			return;
		}
		for (int p : region[lastMove]) {
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