package orego.decision;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static orego.core.Coordinates.*;

public class DumpStats {

	public static void main(String[] args) throws FileNotFoundException {
		DumpStats stat = new DumpStats();
		stat.writeOutput(stat.computeWinrates(), stat.computeAfterForcing());
	}

	private ArrayList<int[]> moves;

	private ArrayList<Integer> result;

	private ArrayList<Integer> turn;

	public DumpStats() {
		try {
			DumpReader reader = new DumpReader();
			reader.read();
			moves = reader.getMoves();
			result = reader.getWinner();
			turn = reader.getTurn();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public double[] computeAfterForcing() {
//		ArrayList<Integer> force = new ArrayList<Integer>();
//		for (int p : new int[] { at("b1"), at("c1"), at("j2"), at("j3"), at("g9"),
//				at("h9"), at("a7"), at("a8") }) {
//			force.add(p);
//		}
		int[] fwins = new int[FIRST_POINT_BEYOND_BOARD];
		int[] fruns = new int[FIRST_POINT_BEYOND_BOARD];
		for (int i = 0; i < turn.size(); i++) { // For each playout
			int win = result.get(i);
			for (int k = 108; k < turn.get(i); k++) { // For each move
				if (k % 2 != 0) {
//					if (force.contains(moves.get(i)[k - 1])) {
					if (moves.get(i)[k - 1] == (at("c1"))) {
						fwins[moves.get(i)[k]] += win;
						fruns[moves.get(i)[k]]++;
					}
				}
			}
		}
		double winrate[] = new double[FIRST_POINT_BEYOND_BOARD];
		for (int i = 0; i < FIRST_POINT_BEYOND_BOARD; i++) {
			winrate[i] = (double) fwins[i] / fruns[i];
			// System.out.println(winrate[i]);
		}
		System.out.println(at("b1"));
		System.out.println("Forced win rate at b1: " + winrate[at("b1")] + " = " + fwins[at("b1")] + "/" + fruns[at("b1")]);
		return winrate;
	}

	public double[] computeWinrates() {
		int[] wins = new int[FIRST_POINT_BEYOND_BOARD];
		int[] runs = new int[FIRST_POINT_BEYOND_BOARD];
		for (int i = 0; i < turn.size(); i++) { // For each playout
			int win = result.get(i);
			// System.out.println("win: " + win);
			for (int k = 108; k < turn.get(i); k++) { // For each move
				// System.out.println(turn.get(i));
				if (k % 2 != 0) {
					// System.out.println("k: " + k);
					wins[moves.get(i)[k]] += win;
					runs[moves.get(i)[k]]++;
				}
			}
		}
		double winrate[] = new double[FIRST_POINT_BEYOND_BOARD];
		for (int i = 0; i < FIRST_POINT_BEYOND_BOARD; i++) {
			winrate[i] = (double) wins[i] / runs[i];
			// System.out.println(winrate[i]);
		}
		System.out.println("General win rate at b1: " + winrate[at("b1")] + " = " + wins[at("b1")] + "/" + runs[at("b1")]);
		return winrate;
	}

	public void writeOutput(double[] normal, double[] force)
			throws FileNotFoundException {
		File file = new File("normal.txt");
		PrintWriter out = new PrintWriter(file);
		for (int i = 0; i < FIRST_POINT_BEYOND_BOARD; i++) {
			if ((!Double.isNaN(normal[i])) && normal[i] > 0.0) {
				out.println(i + " " + normal[i]);
			}
		}
		out.close();
		file = new File("force.txt");
		out = new PrintWriter(file);
		for (int i = 0; i < FIRST_POINT_BEYOND_BOARD; i++) {
			if ((!Double.isNaN(force[i])) && force[i] > 0.0) {
				out.println(i + " " + force[i]);
			}
		}
		out.close();
	}

}
