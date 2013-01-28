package orego.decision;

import static orego.core.Coordinates.at;

public class Experiment2 {

	public static void main(String[] args) {
		String[] problem = { //
				"OOO###OOO", // 9
				"..O###O..", // 8
				"..O###O..", // 7
				"..O###O..", // 6
				"OOO###OOO", // 5
				"#########", // 4
				"##OOOOO##", // 3
				"##O...O##", // 2
				".#O...O#." // 1
		      // ABCDEFGHJ
		};
		int[] testMoves = {
				at("b7"), at("a7"), //at("b8"), at("b6"),
				at("h7"), at("j7"), //at("h8"), at("h6"),
				at("e2"), at("e1"), //at("d2"), at("f2"),
				};
		new Experiment1().run(problem, testMoves);
	}

}
