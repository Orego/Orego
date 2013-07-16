package orego.patternanalyze;

import java.io.Serializable;

public class CopyOfPatternInformation implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int END_GAME = 22;
	long[] runs;
	float[] rate;
	
	public CopyOfPatternInformation() {
		runs = new long[END_GAME+1];
		rate = new float[END_GAME+1];
	}
	
	public CopyOfPatternInformation(float newrate, long newruns, int turn) {
		runs = new long[END_GAME+1];
		rate = new float[END_GAME+1];
		runs[(int)Math.min(turn/20.0, END_GAME)] = newruns;
		rate[(int)Math.min(turn/20.0,END_GAME)] = newrate;
	}

	public long getRuns(int turn) {
		return runs[(int)Math.min(turn/20.0, END_GAME)];
	}
	
	public void setRuns(long runs, int turn) {
		this.runs[(int)Math.min(turn/20.0, END_GAME)] = runs;
	}
	
	public float getRate(int turn) {
		return rate[(int)Math.min(turn/20.0, END_GAME)];
	}
	
	public void setRate(float rate, int turn) {
		this.rate[(int)Math.min(turn/20.0, END_GAME)] = rate;
	}
	
	public void addWin(int turn) {
		rate[(int)Math.min(turn/20.0, END_GAME)] = ((rate[(int)Math.min(turn/20.0, END_GAME)] * runs[(int)Math.min(turn/20.0, END_GAME)]) + 1) / (runs[(int)Math.min(turn/20.0, END_GAME)] + 1);
		runs[(int)Math.min(turn/20.0, END_GAME)]++;
	}
	
	public void addLoss(int turn) {
		rate[(int)Math.min(turn/20.0, END_GAME)] = (rate[(int)Math.min(turn/20.0, END_GAME)] * runs[(int)Math.min(turn/20.0, END_GAME)]) / (runs[(int)Math.min(turn/20.0, END_GAME)] + 1);
		runs[(int)Math.min(turn/20.0, END_GAME)]++;
	}

}
