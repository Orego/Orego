package edu.lclark.orego.genetic;

import static edu.lclark.orego.core.StoneColor.BLACK;

public class Experiment {

	public static void main(String[] args) {
		for (int time : new int[] {1000, 2000, 4000, 8000, 16000, 32000, 64000}) {
			for (int contestants : new int[] {2, 3, 4, 5, 6}) {
				int count = 0;
				for (int trial = 0; trial < 50; trial++) {
					Player player = new PlayerBuilder().populationSize(2000).individualLength(2000).msecPerMove(time).threads(32).boardWidth(5).contestants(contestants).openingBook(false).build();
					String[] diagram = {
							"#.#.#",
							"#####",
							".....",
							"OOOOO",
							"O...O", };
					player.getBoard().setUpProblem(diagram, BLACK);
					if (player.getBoard().getCoordinateSystem().at("c1") == player.bestMove()) {
						count++;
					}
				}
				System.out.println(time + " msec, " + contestants + " contestants: " + count + "/50");
			}
		}
	}

}
