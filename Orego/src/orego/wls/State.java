package orego.wls;

import java.util.Hashtable;




/** A given state or "value" of a WLS indicator. There are a finite
 * number of states which are linked together into a tree (directed of course).
 * TODO: should states be able to transition themselves (for example, call addWin or addLoss
 * on WinLossStates and then adjust accordingly)
 */
public class State implements Comparable<State> {
	private int wins;

	private int runs;
	
	private double confidence;
	
	// precomputed z-values
	private static Hashtable<Double, Double> z_values = new Hashtable<Double, Double>();
	
	static {
		storeZValues();
	}
	
	protected static void storeZValues() {
		z_values.clear();
		
		// currently just create a table
		z_values.put(.9500,  1.96); // 95% confidence level
		z_values.put(.9000,  1.65); // 90% confidence level
		z_values.put(.74857,  .674490); // 75% confidence level
		z_values.put(.75,     .674490);
	}
	
	protected double getZValue(double confidence_level) {
		if (z_values.containsKey(confidence_level))
			return z_values.get(confidence_level);
	
		throw new UnsupportedOperationException("Entry for confidence level: " + confidence_level + " doesn't exist!");
	}
	
	public State(int wins, int runs) {
		this.wins = wins;
		this.runs = runs;
		
		// you can recompute later if you wish
		this.computeConfidence(WinLossStates.WIN_THRESHOLD); // TODO: probably better to do dependency injection instead of static field
		
	}
	
	
	
	@Override
	public int compareTo(State that) {
		if (this.getConfidence() < that.getConfidence()) 
			return -1;
		
		if (this.getConfidence() > that.getConfidence())
			return 1;
		
		return 0;
	}
	
	
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	public double getConfidence() {
		return this.confidence;
	}
	
	public void setWins(int wins) {
		this.wins = wins;
	}
	
	public int getWins() {
		return this.wins;
	}
	
	public void setRuns(int runs) {
		this.runs = runs;
	}
	
	public int getRuns() {
		return this.runs;
	}
	
	public int getLosses() {
		return getRuns() - getWins();
	}
	
	/**
	 * Calculates the wins / runs proportion
	 * @return A real valued positive number or -1 if we are 0 / 0 
	 */
	public double getWinRunsProportion() {
		if (runs == 0) return -1; // occurs if state is 0/0.
		
		return ((double) wins / (double) runs);
	}
	
	/**
	 * Computes the confidence that this proportion is above
	 * or below the win_threshold. We use the Agresti-Coull interval
	 * to compensate for smaller number of runs. We also want
	 * numbers with more runs to have more "weight" as they are statistically 
	 * stronger and hence represent the proportion "better".
	 * Depending on whether or not our proportion is above or below the win_threshold we calculate
	 * the lower or upper bound, respectively. 
	 * 
	 * See paper for calculation of confidence interval.
	 * 
	 * Note: this confidence calculation resembles a "rating". We are calculating a value between [-1, 1] indicating
	 * our confidence that a value is above or below 1/2. If the value is below 1/2, then we have a confidence range of
	 * 0 to -1. If the proportion is above 1/2 then we have a confidence range of 0 to 1.
	 * 
	 * When the proportion is below 1/2, then "confidence" gets progressively worse as the number of runs increases.
	 * For example 2/8 is "worse" than 1/4. By worse we mean a more "negative" result indicating our *increased* confidence
	 * that the proportion is *below* 1/2. 
	 * 
	 * On the other hand, if the proportion is above 1/2 then the confidence becomes increasingly positive as the number
	 * of runs (samples) increases. Hence, 12/16 is much better than 3/4. In other words, we are "more confident" that 12/16 is 
	 * above 1/2 than 3/4 is above one half.
	 * 
	 * TODO: perhaps convert to logs for better performance?
	 * @param win_threshold Usually 1/2. The threshold we are computing our confidence interval around.
	 */
	public void computeConfidence(double win_threshold) {
		double z_value = getZValue(WinLossStates.CONFIDENCE_LEVEL);
		double z_value_squared = z_value * z_value;

		// variables are named according to paper
		// Refer to article for additional definition
		double mhat = runs + z_value_squared;
		double phat = (wins + .5 * (z_value_squared)) / mhat;
		
		double CI   = z_value * Math.sqrt((phat * (1 - phat)) / mhat);

		double bound = phat;
		
		// now add or subtract based on whether we are computing lower or upper bound
		if (getWinRunsProportion() >= WinLossStates.WIN_THRESHOLD)
			bound -= CI; // provide the lower bound of the entire *interval* which is itself above .5
		else {
			bound += CI; // provide the upper bound of the entire *interval* which is itself below .5
			// we shift by a constant amount to avoid overlap.
			// Remember, each confidence interval (upper and lower bound) usually lie above or below .5
			// However, there are cases where the lowerbound of an interval above .5 could overlap with the upperbound
			// of an interval below .5. To remedy this we shift the upperbounds of the intervals below .5 down by 1 to ensure this doesn't occur.
			// Similarly you could bump the lower bounds of intervals above .5 by 1 to ensure no overlap
			bound -= 1;
		}

		this.confidence = bound;
	}
	
	@Override
	public String toString() {
		return String.format("\nWins: %d\nRuns: %d\nLosses: %d\n Confidence: %.3f\n", wins, runs, getLosses(), confidence);
	}
}
