package bandit;

import ec.util.MersenneTwisterFast;

public class MetaTable {

	public static void main(String[] args) {
		int totalNoise = 1024 * 1024;
		int numberOfTables = 1;
		int tableSize = 17; // In bits
		int best = 23;
		int secondBest = 42;
		Table[] tables = new Table[numberOfTables];
		for (int i = 0; i < tables.length; i++) {
			tables[i] = new Table(tableSize);
		}
		MersenneTwisterFast random = new MersenneTwisterFast();
		for (int j = 0; j < tables.length; j++) {
			// The best entry has a 100% win rate
			for (int i = 0; i < 100; i++) {
				tables[j].store(best, true);
			}
			// The second best entry has a 90% win rate
			for (int i = 0; i < 90; i++) {
				tables[j].store(secondBest, true);
			}
			for (int i = 0; i < 10; i++) {
				tables[j].store(secondBest, false);
			}
			// Many other moves have 50% win rates
			for (int i = 0; i < totalNoise / numberOfTables; i++) {
				tables[j].store(random.nextInt(), true);
				tables[j].store(random.nextInt(), false);
			}
		}
		for (int i = 0; i < tables[0].getWinRates().length; i++) {
			double sum = 0.0;
			for (int k = 0; k < tables.length; k++) {
				sum += tables[k].getWinRates()[i];
			}
			System.out.println(i + "\t" + (sum / tables.length));
		}
	}

	
}
