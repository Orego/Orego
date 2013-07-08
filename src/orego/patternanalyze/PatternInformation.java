package orego.patternanalyze;

import java.io.Serializable;

public class PatternInformation implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int runs;
	float rate;
	String stringVersion;
	
	public PatternInformation() {
		runs = 0;
		rate = 0.0f;
		stringVersion = "";
	}
	
	public PatternInformation(float newrate, int newruns, String newStringVersion) {
		runs = newruns;
		rate = newrate;
		stringVersion = newStringVersion;
	}

	public int getRuns() {
		return runs;
	}
	
	public void setRuns(int runs) {
		this.runs = runs;
	}
	
	public float getRate() {
		return rate;
	}
	
	public void setRate(float rate) {
		this.rate = rate;
	}
	
	
	public String toString() {
		return stringVersion;
	}

}
