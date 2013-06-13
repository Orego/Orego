package orego.heuristic;

import static orego.core.Coordinates.at;
import orego.play.UnknownPropertyException;

/** Recommends a given point. For unit testing. */
public class SpecificPointHeuristic extends Heuristic {

	/** The point suggested by this heuristic. */
	private int specificPoint;

	/**
	 * By default, the specific point is c5. This can be changed using {@link #setProperty(String, String)}.
	 */
	public SpecificPointHeuristic(int weight) {
		super(weight);
		specificPoint = at("c5");
		recommend(specificPoint);
	}

	@Override
	public SpecificPointHeuristic clone() {
		SpecificPointHeuristic result = (SpecificPointHeuristic) super.clone();
		result.specificPoint = specificPoint;
		return result;
	}

	/** Returns the specific point suggested by this heuristic. */
	public int getSpecificPoint() {
		return specificPoint;
	}

	@Override
	public void setProperty(String name, String value)
			throws UnknownPropertyException {
		if (name.equals("specificPoint")) {
			specificPoint = Integer.parseInt(value);
			recommend(specificPoint);
		} else {
			super.setProperty(name, value);
		}
	}

}
