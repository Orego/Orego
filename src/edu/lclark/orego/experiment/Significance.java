package edu.lclark.orego.experiment;

import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.util.Scanner;

/**
 * Tests for significant differences in win rates between different conditions.
 * The test is a two-tailed test for difference of proportions. The three
 * required values (total number of games and number of wins in each condition)
 * can be entered on the command line or interactively.
 */
public final class Significance {

	@SuppressWarnings("boxing")
	public static void main(String[] args) {
		int n;
		int wins1;
		int wins2;
		if (args.length == 3) {
			n = parseInt(args[0]);
			wins1 = parseInt(args[1]);
			wins2 = parseInt(args[2]);
		} else {
			try (Scanner in = new Scanner(System.in)) {
				System.out
						.println("Enter number of runs per condition, wins in condition 1, and wins in condition 2, separated by whitespace:");
				n = in.nextInt();
				wins1 = in.nextInt();
				wins2 = in.nextInt();
			}
		}
		final double p1 = (double) wins1 / n;
		final double p2 = (double) wins2 / n;
		final double z = zScore(p1, p2, n);
		System.out.println("Z score: " + z);
		if (abs(z) > 1.96) {
			System.out.println("The difference is SIGNIFICANT (p < 0.05)");
		} else {
			System.out.println("The difference is NOT significant");
		}
		System.out.printf(
				"The confidence interval for condition 1 is %1.3f +/- %1.3f\n",
				p1, margin(p1, n));
		System.out.printf(
				"The confidence interval for condition 2 is %1.3f +/- %1.3f\n",
				p2, margin(p2, n));
	}

	/**
	 * Returns the width of the 95% confidence interval for win rate p with n
	 * games.
	 */
	public static double margin(double p, int n) {
		return 1.96 * sqrt(p * (1 - p) / n);
	}

	/**
	 * Returns the z score.
	 *
	 * @param p1
	 *            Win rate for condition 1.
	 * @param p2
	 *            Win rate for condition 2.
	 * @param n
	 *            Number of games.
	 */
	public static double zScore(double p1, double p2, int n) {
		final double p = (p1 + p2) / 2;
		return (p1 - p2) / sqrt(p * (1 - p) * (2.0 / n));
	}

}
