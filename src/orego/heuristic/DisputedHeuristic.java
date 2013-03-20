package orego.heuristic;

import orego.core.Board;
import static orego.core.Colors.*;
import orego.core.Coordinates;
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
		/* Point the neighborhood array to the KNIGHT_NEIGHBORHOOD array */
		try {
			setNeighborhood("largeknight");
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
		}
	}
	
	public void setThreshold(double newThreshold) {
		this.threshold = newThreshold;
	}
	
	public void setMaxVacancies(int newMax) {
		this.maxVacancies = newMax;
	}
	
	public void setNeighborhood(String str) throws UnknownPropertyException {
		if (str.equals("largeknight")) neighborhood = Coordinates.LARGE_KNIGHT_NEIGHBORHOOD;
		else if (str.equals("knight")) neighborhood = Coordinates.KNIGHT_NEIGHBORHOOD;
		else if (str.equals("adjacent")) neighborhood = Coordinates.NEIGHBORS;
		else throw new UnknownPropertyException("Invalid neighborhood: " + str);
	}
	
	@Override
	public void prepare(Board board) {
		super.prepare(board);
		IntSet vacantPoints = board.getVacantPoints();
		if (vacantPoints.size() <= maxVacancies) {
			for (int i = 0; i < vacantPoints.size(); i++) {
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
		} else if (name.equals("neighborhood")) {
			setNeighborhood(value);
		} else {
			super.setProperty(name, value);
		}
	}
	
}
