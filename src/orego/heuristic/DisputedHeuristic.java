package orego.heuristic;

import static orego.core.Coordinates.*;
import orego.core.Board;
import static orego.core.Colors.*;
import orego.util.IntSet;

public class DisputedHeuristic extends Heuristic {

	private double threshold;
	
	public DisputedHeuristic(int weight, double threshold) {
		super(weight);
		this.threshold = threshold;
	}
	
	public DisputedHeuristic(int weight) {
		this(weight, 0.21);	// 0.21 seems to be a good default
	}
	
	@Override
	public void prepare(Board board) {
		super.prepare(board);
		IntSet vacantPoints = board.getVacantPoints();
		for (int i = 0; i < vacantPoints.size(); i++) {
			int numBlack = 0;
			int numWhite = 0;
			int[] neighborhood = LARGE_KNIGHT_NEIGHBORHOOD[vacantPoints.get(i)];
			for (int j = 0; j < neighborhood.length; j++) {
				int color = board.getColor(neighborhood[j]);
				if (color == BLACK) numBlack++;
				else if (color == WHITE) numWhite++;
			}
			if (numBlack + numWhite > 0) {
				double p = (double) numBlack / (numBlack + numWhite);
				if (p * (1 - p) > threshold) {
					recommend(vacantPoints.get(i));
				}
			}
		}
	}
	
	@Override
	public DisputedHeuristic clone() {
		DisputedHeuristic copy = (DisputedHeuristic) super.clone();
		copy.threshold = this.threshold;
		return copy;
	}

}
