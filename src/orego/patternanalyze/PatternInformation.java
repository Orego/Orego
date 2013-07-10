package orego.patternanalyze;

import java.io.Serializable;

public class PatternInformation implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	long runs;
	float rate;
	
	public PatternInformation() {
		runs = 0;
		rate = 0.0f;
	}
	
	public PatternInformation(float newrate, long newruns) {
		runs = newruns;
		rate = newrate;
	}

	public long getRuns() {
		return runs;
	}
	
	public void setRuns(long runs) {
		this.runs = runs;
	}
	
	public float getRate() {
		return rate;
	}
	
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	public void addWin() {
		rate = ((rate * runs) + 1) / (runs + 1);
		runs++;
	}
	
	public void addLoss() {
		rate = (rate * runs) / (runs + 1);
		runs++;
	}

}
