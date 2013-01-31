package orego.patterns;

public class RatedPattern implements Comparable<RatedPattern> {

	private char pattern;
	
	private double ratio;

	public RatedPattern(char pattern, double ratio) {
		this.pattern = pattern;
		this.ratio = ratio;
	}
	
	public int compareTo(RatedPattern that) {
		if (ratio < that.ratio) {
			return -1;
		} else if (ratio == that.ratio) {
			return 0;
		} else {
			return 1;
		}
	}

	public char getPattern() {
		return pattern;
	}

	public double getRatio() {
		return ratio;
	}

}
