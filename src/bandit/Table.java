package bandit;

import ec.util.MersenneTwisterFast;

/** Stores, at each position, a win rate in [0.0, 1.0] and a number of runs. */
public class Table {

	private double[] winRates;
	
	private long[] runCounts;
	
	private int mask;
	
	public Table(int bits) {
		winRates = new double[1 << bits];
		runCounts = new long[1 << bits];
		mask = (1 << bits) - 1;
	}

	/**
	 * Store a win or loss at the indicated hash.
	 * @param hash The hash entry to update.
	 * @param win True for a win, false for a loss.
	 */
	public void store(int hash, boolean win) {
		int h = hash & mask;
		if (win) {
			winRates[h] = (winRates[h] * runCounts[h] + 1) / (runCounts[h] + 1);
		} else {
			winRates[h] = (winRates[h] * runCounts[h]) / (runCounts[h] + 1);
		}
		runCounts[h]++;
	}
	
	/**
	 * Returns the win rate at the specified hash.
	 */
	public double getWinRate(int hash) {
		int h = hash & mask;
		if (runCounts[h] == 0) {
			return 0.5;
		}
		return winRates[h];
	}

	protected double[] getWinRates() {
		return winRates;
	}

	protected long[] getRunCounts() {
		return runCounts;
	}

	/**
	 * Returns the run count at the specified hash.
	 */
	public long getRunCount(int hash) {
		int h = hash & mask;
		return runCounts[h];
	}

	public static void main(String[] args) {
		Table t = new Table(17);
		MersenneTwisterFast random = new MersenneTwisterFast();
		int entry = 23;
		for (int i = 0; i < 100; i++) {
			t.store(entry, true);
		}
		for (int i = 0; i < 1000000; i++) {
			t.store(random.nextInt(), true);
			t.store(random.nextInt(), false);
		}
//		System.out.println(t.getWinRate(23));
		for (int i = 0; i < t.winRates.length; i++) {
			System.out.println(i + "\t" + t.getWinRate(i));
		}
	}

}
