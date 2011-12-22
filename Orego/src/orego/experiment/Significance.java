package orego.experiment;

import java.util.*;
import static java.lang.Math.*;

/**
 * Tests for significant differences in win rates between different conditions.
 * The test is a two-tailed test for difference of proportions.
 */
public class Significance {

	public static double zScore(double p1, double p2, int n) {
		double p = (p1 + p2) / 2;
		return (p1 - p2) / sqrt(p * (1 - p) * (2.0 / n));
	}

	public static double margin(double p, int n) {
		return 1.96 * sqrt(p * (1 - p) / n);
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out
				.println("Enter number of runs per condition, wins in condition 1, and wins in condition 2, separated by whitespace:");
		int n = in.nextInt();
		int wins1 = in.nextInt();
		int wins2 = in.nextInt();
		double p1 = ((double) wins1) / n;
		double p2 = ((double) wins2) / n;
		double z = zScore(p1, p2, n);
		System.out.println("Z score: " + z);
		if (abs(z) > 1.96) {
			System.out.println("The difference IS significant (p < 0.05)");
		} else {
			System.out.println("The difference IS NOT significant");
		}
		System.out.printf(
				"The confidence interval for condition 1 is %1.3f +/- %1.3f\n",
				p1, margin(p1, n));
		System.out.printf(
				"The confidence interval for condition 2 is %1.3f +/- %1.3f\n",
				p2, margin(p2, n));
	}

}
