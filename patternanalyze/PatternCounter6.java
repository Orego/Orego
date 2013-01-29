package orego.patternanalyze;

import static orego.core.Coordinates.*;

public class PatternCounter6 extends PatternCounter5 {
	
	public static void main(String[] args) {
		new PatternCounter6().run();
	}
	
	public PatternCounter6() {
		super();
		outputFile = "BadPatterns2";
	}

	@Override
	protected int[] getPointsToAnalyze(int p) {
		return ALL_POINTS_ON_BOARD;
	}
	
}
