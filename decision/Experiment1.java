package orego.decision;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;

public class Experiment1 {

	public static void main(String[] args) {
		String[] problem = { //
				".OO#O....", // 9
				".OO#OOOOO", // 8
				".O######O", // 7
				".O#.#..##", // 6
				"OO##..#OO", // 5
				"##....#O.", // 4
				"O######O.", // 3
				"OOOOO#OO.", // 2
				"....O#OO." // 1
		      // ABCDEFGHJ
		};
		int[] testMoves = { at("c1"), at("b1"), at("a7"), at("a8"), at("g9"),
				at("h9"), at("j3"), at("j2") };
		new Experiment1().run(problem, testMoves);
	}

	public void run(String[] problem, int[] testMoves) {
		try {
			int runs = 200;
			int mctsCount = 0;
			int treeCount = 0;
			int i = 0;
			for (i = 0; i < runs; i++) {
				System.out.println("Run " + i);
				// Test MCTS using the DumpPlayer
				// This also produces the playouts for the decision trees
				DumpPlayer player = new DumpPlayer();
//				player.setProperty("policy", "Random");
				player.setProperty("policy", "Escape:Pattern:Capture");
				player.setProperty("threads", "1");
				player.setProperty("priors", "20");
				player.reset();
				player.openFile();
				player.setPlayoutLimit(10000);
				player.setUpProblem(BLACK, problem);
				if (player.getTurn() % 2 == 1) {
					// TODO This is a kludge dealing with the inconsistency of
					// move parity and a board's colorToPlay field
					// Should we remove one to avoid redundancy?
					player.getBoard().setColorToPlay(WHITE);
					player.acceptMove(PASS);
				}
				assert ((player.getTurn() % 2 == 0) == (player.getBoard().getColorToPlay() == BLACK));
				assert (player.getBoard().getColorToPlay() == BLACK);
//				System.out.println(colorToString(player.getBoard().getColorToPlay()));
//				System.out.println(player.getTurn());
//				System.out.println(pointToString(player.getBoard().getMove(player.getTurn() - 1)));
//				System.out.println(pointToString(player.getBoard().getMove(player.getTurn() - 2)));
				int startMove = player.getTurn();
				player.bestMove();
				player.closeFile();
				player.setPlayoutLimit(0);
				if (player.testOnce(testMoves)) {
					mctsCount++;
				}
				// Now test the decision trees
				treeCount += new DumpReader1().process(problem, testMoves, startMove);
			}
			System.out.println("DumpPlayer: " + ((double) mctsCount) / (i + 1) + " "
					+ mctsCount + "/" + (i + 1));
			System.out.println("Decision trees: " + ((double) treeCount) / (i + 1) + " "
					+ treeCount + "/" + (i + 1));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
