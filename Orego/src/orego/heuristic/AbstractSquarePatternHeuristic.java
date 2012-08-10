package orego.heuristic;

import static orego.core.Colors.VACANT;
import static orego.core.Coordinates.ON_BOARD;
import static orego.core.Coordinates.SQUARE_NEIGHBORHOOD;

import java.util.Arrays;

import orego.core.Board;
import orego.play.UnknownPropertyException;

/**
 * Just like PatternHeuristic but looks in a square region around the last move.
 */
public abstract class AbstractSquarePatternHeuristic extends AbstractPatternHeuristic {
	
	/** Reference to static entry in table SQUARE_NEIGHBORHOOD*/
	private int[][] region;

	private int radius;
	
	public AbstractSquarePatternHeuristic(int weight) {
		super(weight);
	}

	protected void setRegion(int[][] region) {
		this.region = region;
	}
	
	protected int[][] getRegion() {
		return region;
	}
	
	protected void setRadius(int radius) {
		this.radius = radius;
		setRegion(SQUARE_NEIGHBORHOOD[radius]);
	}
	
	protected int getRadius() {
		return radius;
	}

	@Override
	public void setProperty(String property, String value) throws UnknownPropertyException {
		super.setProperty(property, value);
		if (property.equals("radius")) {
			radius = Integer.valueOf(value);
			region = SQUARE_NEIGHBORHOOD[radius];
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
				if (GOOD_NEIGHBORHOODS[board.getColorToPlay()]
						.get(neighborhood)) {
					recommend(p);
				}
				if (BAD_NEIGHBORHOODS[board.getColorToPlay()].get(neighborhood)) {
					discourage(p);
				}
			}
		}
	}

	@Override
	public AbstractSquarePatternHeuristic clone() {
		AbstractSquarePatternHeuristic copy = (AbstractSquarePatternHeuristic) super.clone();
		
		copy.setRadius(this.radius);
		return copy;
	}
}
