package orego.heuristic;

import static orego.core.Coordinates.at;
import orego.core.Board;
import orego.core.Coordinates;
import orego.play.UnknownPropertyException;

/** Recommands a given point. For unit testing. */
public class SpecificPointHeuristic extends Heuristic {

	private int specificPoint = Coordinates.NO_POINT;
	
	public SpecificPointHeuristic(int weight) {
		super(weight);
		recommend(at("c5"));
	}

	@Override
	public void prepare(Board board) {
		// Does nothing
	}
	
	@Override
	public void setProperty(String name, String value) throws UnknownPropertyException {
		if (name.equals("specificPoint")) {
			specificPoint = Integer.parseInt(value);
		} else {
			super.setProperty(name, value);
		}
	}
	
	public int getSpecificPoint() {
		return specificPoint;
	}
	
	@Override
	public SpecificPointHeuristic clone() {
		SpecificPointHeuristic sph = (SpecificPointHeuristic) super.clone();
		
		try {
			sph.setProperty("specificPoint", String.valueOf(specificPoint));
		} catch (UnknownPropertyException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return sph;
	}
	
	
	
}
