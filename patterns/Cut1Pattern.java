package orego.patterns;

import static orego.core.Colors.*;

/**
 * Gelly et al's Cut1 pattern. This requires special code because it is of
 * the form "matches this but not that".
 */
public class Cut1Pattern extends Pattern {

	public boolean matches(char neighborhood) {
		for (int color = BLACK; color < NUMBER_OF_PLAYER_COLORS; color++) {
			// Neighborhood must NOT match this...
			setColors("OOO.#???", color);
			if (countsAsInAnyOrientation(neighborhood)) {
				continue;
			}
			// ...but must match this
			setColors("OO??#???", color);
			if (countsAsInAnyOrientation(neighborhood)) {
				return true;
			}
		}
		return false;
	}

}
