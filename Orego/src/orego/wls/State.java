package orego.wls;

import java.util.Hashtable;




/** A given state or "value" of a WLS indicator. There are a finite
 * number of states which are linked together into a tree (directed of course)
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
	
	private static void storeZValues() {
		z_values.clear();
		
		// currently just create a table
		z_values.put(.97500, 1.96); // 95% confidence level
		z_values.put(.95000, 1.65); // 90% confidence level
		z_values.put(.74857,  .674490);
	}
	
	public State(int wins, int runs) {
		this.wins = wins;
		this.runs = runs;
		
		// you can recompute later if you wish
		this.computeConfidence(WinLossStates.WIN_THRESHOLD);
		
	}
	
	
	
	@Override
	public int compareTo(State that) {
		if (this.confidence < that.confidence) {
			return -1;
		}
		if (this.confidence > that.confidence) {
			return 1;
		}
		
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
	
	public double getWinRunsProportion() {
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
	 * See paper for calculation of confidence interval
	 * 
	 * TODO: perhaps convert to logs for better performance?
	 * @param win_threshold
	 * @return
	 */
	public double computeConfidence(double win_threshold) {
		double z_value = z_values.get(WinLossStates.CONFIDENCE_LEVEL);
		double z_value_squared = z_value * z_value;

		// variables are named according to paper
		// Refer to article for additional definition
		double mhat = runs + z_value_squared;
		double phat = (wins + .5 * (z_value_squared)) / mhat;
		
		double CI   = z_value * Math.sqrt((phat * (1 - phat)) / mhat);

		double bound = phat;
		
		// now add or subtract based on whether we are computing lower or upper bound
		if (getWinRunsProportion() >= WinLossStates.WIN_THRESHOLD)
			bound -= CI;
		else
			bound += CI;
		
		return bound;
	}
}
