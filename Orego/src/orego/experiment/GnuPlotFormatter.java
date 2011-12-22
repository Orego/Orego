package orego.experiment;

import java.util.*;
import java.io.*;

public class GnuPlotFormatter {

	public static void main(String[] args) throws FileNotFoundException {
		// Build data structures
		double[] learn = { 0.04, 0.05, 0.06 };
		double[] biasAlpha = { 1, 2, 3 };
		double[] prevAlpha = { 1, 2, 3 };
		double[] penultAlpha = { 1, 2, 3 };
		double[][][][] data = new double[learn.length][biasAlpha.length][prevAlpha.length][penultAlpha.length];
		// Read data from file
		Scanner in = new Scanner(new File("raw-data.txt"));
		while (in.hasNextLine()) {
			String line = in.nextLine();
			int a = findIndex(learn, line, "learn=");
			int b = findIndex(biasAlpha, line, "biasAlpha=");
			int c = findIndex(prevAlpha, line, "prevAlpha=");
			int d = findIndex(penultAlpha, line, "penultAlpha=");
			data[a][b][c][d] = Double.parseDouble(line.substring(line
					.lastIndexOf(' ') + 1));
		}
		// Print data
		for (int le = 0; le < learn.length; le++) {
			System.out.print(learn[le] + "\t");
			for (int bA = 0; bA < biasAlpha.length; bA++) {
				for (int prA = 0; prA < prevAlpha.length; prA++) {
					for (int peA = 0; peA < penultAlpha.length; peA++) {
						System.out.print(data[le][bA][prA][peA] + "\t");
					}
				}
			}
			System.out.println();
		}
		// Print gnuplot commands
		System.out.println("set style data linespoints");
		String command = "plot ";
		int column = 2;
		for (int bA = 0; bA < biasAlpha.length; bA++) {
			for (int prA = 0; prA < prevAlpha.length; prA++) {
				for (int peA = 0; peA < penultAlpha.length; peA++) {
					if (column > 2) {
						command += ", ";
					}
					command += "'data.txt' using 1:" + column
							+ " title 'bi" + biasAlpha[bA] + "pr"
							+ prevAlpha[prA] + "pe" + penultAlpha[peA]
							+ "'";
					column++;
				}
			}
		}
		System.out.println(command);
	}

	protected static int findIndex(double[] hidden, String line, String label) {
		int start = line.indexOf(label);
		start += label.length();
		int end = Math.min(line.indexOf(' ', start), line.indexOf(':', start));
		double h = Double.parseDouble(line.substring(start, end));
		return java.util.Arrays.binarySearch(hidden, h);
	}
}
