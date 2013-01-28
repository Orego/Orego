package orego.decision;

import static orego.core.Coordinates.at;

public class Experiment3 {

	public static void main(String[] args) {
		String[] problem = { //
				"O.O#.O...", // 9
				".OO##OO..", // 8
				"O.OO#O##.", // 7
				"OOOO#OOO.", // 6
				"##OO####.", // 5
				"#####....", // 4
				"OO####...", // 3
				"OOOOO#.#.", // 2
				"....O#..." // 1
		      // ABCDEFGHJ
		};
		int[] testMoves = { at("j6"), at("j8"), //at("j7"), at("h8"), at("j5"), at("h9"),
				at("b1"), at("c1") };
		new Experiment1().run(problem, testMoves);
	}

}
