package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.ShortList;
import static edu.lclark.orego.core.CoordinateSystem.*;

/** Keeps track of how many stones there are of each color. */
public class StoneCounter implements BoardObserver {

	private final int[] counts;
	
	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		if (location != PASS) {
			counts[color.index()]++;
			counts[color.opposite().index()] -= capturedStones.size();
		}
	}
	
	public StoneCounter(Board board) {
		counts = new int[2];
		board.addObserver(this);
	}

	/** Returns the number of stones of this color. */
	public int getCount(StoneColor color) {
		return counts[color.index()];
	}

}
