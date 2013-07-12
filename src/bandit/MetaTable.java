package bandit;

import ec.util.MersenneTwisterFast;

public class MetaTable {

	public static void main(String[] args) {
		Table[] tables = new Table[512]; // # of tables
		for (int i = 0; i < tables.length; i++) {
			tables[i] = new Table(8); // Size of tables
		}
		MersenneTwisterFast random = new MersenneTwisterFast();
		int entry = 23;
		for (int j = 0; j < tables.length; j++) {
			for (int i = 0; i < 100; i++) {
				tables[j].store(entry, true);
			}
			// The move next to entry has a 90% win rate
			for (int i = 0; i < 90; i++) {
				tables[j].store(entry + 1, true);
			}
			for (int i = 0; i < 10; i++) {
				tables[j].store(entry + 1, false);
			}
			for (int i = 0; i < (1024 * 1024) / tables.length; i++) {
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
