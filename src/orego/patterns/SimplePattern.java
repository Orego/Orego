package orego.patterns;

import static orego.core.Colors.*;

/** The most common type of pattern, matching for either color. */
public class SimplePattern extends Pattern {

	/** Colors for this pattern. */
	private String specification;

	/** @see orego.patterns.Pattern#setColors */
	public SimplePattern(String specification) {
		this.specification = specification;
	}

	/** Returns the specification for this pattern. */
	protected String getSpecification() {
		return specification;
	}

	@Override
	public boolean matches(char neighborhood) {
		for (int color = BLACK; color < NUMBER_OF_PLAYER_COLORS; color++) {
			setColors(specification, color);
			if (countsAsInAnyOrientation(neighborhood)) {
				return true;
			}
		}
		return false;
	}

}
