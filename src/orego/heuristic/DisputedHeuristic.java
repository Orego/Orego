package orego.heuristic;

import static orego.core.Coordinates.*;
import orego.core.Board;
import static orego.core.Colors.*;
import orego.play.UnknownPropertyException;
import orego.util.IntSet;

public class DisputedHeuristic extends Heuristic {

	protected double threshold;
	protected int maxVacancies;
	protected int[][] neighborhood;
	
	public DisputedHeuristic(int weight) {
		super(weight);
		setThreshold(0.21);		// these might be good defaults
		setMaxVacancies(50);	// I'm currenly looking for better ones
		/* Point the neighborhood array to the LARGE_KNIGHT_NEIGHBORHOOD array */
		this.neighborhood = LARGE_KNIGHT_NEIGHBORHOOD;
	}
	
	public void setThreshold(double newThreshold) {
		this.threshold = newThreshold;
	}
	
	public void setMaxVacancies(int newMax) {
		this.maxVacancies = newMax;
	}
	
	@Override
	public void prepare(Board board) {
		super.prepare(board);
		IntSet vacantPoints = board.getVacantPoints();
		if (vacantPoints.size() <= maxVacancies) {
			for (int i = 0; i < vacantPoints.size(); i	++) {
				int numBlack = 0;	
				int numWhite = 0;
				int[] neighborhoodAtPoint = neighborhood[vacantPoints.get(i)];
				for (int j = 0; j < neighborhoodAtPoint.length; j++) {
					int color = board.getColor(neighborhoodAtPoint[j]);
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
	}
	
	@Override
	public DisputedHeuristic clone() {
		DisputedHeuristic copy = (DisputedHeuristic) super.clone();
		copy.threshold = this.threshold;
		return copy;
	}

	@Override
	public void setProperty(String name, String value) throws UnknownPropertyException {
		if (name.equals("threshold")) {
			setThreshold(Double.valueOf(value));
		} else if (name.equals("maxVacancies")) {
			setMaxVacancies(Integer.parseInt(value));
		} else {
			super.setProperty(name, value);
		}
	}
	
}
