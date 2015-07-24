package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.StoneColor.BLACK;
import static edu.lclark.orego.experiment.GameBatch.timeStamp;
import static edu.lclark.orego.experiment.Git.getGitCommit;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static edu.lclark.orego.experiment.SystemConfiguration.SYSTEM;
import static java.io.File.separator;

import java.io.File;
import java.io.PrintWriter;

import edu.lclark.orego.experiment.Broadcast;

public class Experiment {

	public static void main(String[] args) throws Exception {
		// Create results directories
		final String resultsDirectory = SYSTEM.resultsDirectory
				+ timeStamp(true) + separator;
		System.out
				.println("Results will be stored in "
						+ resultsDirectory);
		new File(resultsDirectory).mkdirs();
		// Note which git commit we're using
		final String gitCommit = getGitCommit();
		if (gitCommit.isEmpty()) {
			throw new IllegalStateException("Not in clean git state");
		}
		try (PrintWriter out = new PrintWriter(resultsDirectory + "git.txt")) {
			out.println(gitCommit);
		}
		// Note system properties
		Broadcast.copyFile(OREGO_ROOT + "config" + separator + "system.properties",
				resultsDirectory + "system.txt");
		// Run experiment
		final String outFile = resultsDirectory + "results-"
				+ timeStamp(false) + ".txt";
		try (PrintWriter out = new PrintWriter(outFile)) {
			System.out.println("Inside try block");
//			for (int time : new int[] {100, 200}) {
//				for (int contestants : new int[] {2, 6}) {
					for (int time : new int[] {0, 250, 500, 1000, 2000, 4000, 8000}) {
						for (int contestants : new int[] {16, 4}) {
							for (int indSize : new int[] {20, 200}) {
								for (int popSize: new int[] {2000, 4000}) {
					int count = 0;
					for (int trial = 0; trial < 50; trial++) {
//						System.out.println("Inside innermost loop");
//						out.println("Starting trial...");
//						out.flush();
						Player player = new PlayerBuilder().populationSize(0).individualLength(0).msecPerMove(time).threads(32).boardWidth(9).contestants(contestants).openingBook(false).build();
//						String[] diagram = {
//								"#.#.#####",
//								"#########",
//								"#########",
//								"#########",
//								".........",
//								"OOOOOOOOO",
//								"OOOOOOOOO",
//								"OOOOOOOOO",
//								"O...OOOOO", };
						String[] diagram = {
								".#######.",
								"#########",
								"#########",
								"#########",
								"OOOOOOOOO",
								"OOOOOOOOO",
								"OOO..OOOO",
								"OOO...OOO",
								"OOOO.OOOO",
								};
						player.getBoard().setUpProblem(diagram, BLACK);
						player.createPopulations(popSize, indSize);
						if (player.getBoard().getCoordinateSystem().at("e2") == player.bestMove()) {
							count++;
						}
//						out.println("...finished");
//						out.flush();
					}
					out.println(time + " msec, " + contestants + " contestants, " + indSize + " ind size, " + popSize + " pop size: " + count + "/50");
					out.flush();
				}
			}
						}
					}
		}
	}

}
