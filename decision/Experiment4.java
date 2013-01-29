package orego.decision;

import static orego.core.Coordinates.*;

public class Experiment4 {

	public static void main(String[] args) {
		String[] problem = { //
				"O.O#.O...", // 9
				".OO##OO..", // 8
				"O.OO#O##.", // 7
				"OOOO#OOO.", // 6
				"##OO####.", // 5
				".#####...", // 4
				".#OOO.#..", // 3
				"OO..O#.#.", // 2
				"........." // 1
		      // ABCDEFGHJ
		};
		int[] testMoves = { at("j6"), at("j8"), // at("j7"), at("h8"), at("j5"), at("h9"),
				at("d1"), at("e1"), at("b1"), at("c2") };
		new Experiment1().run(problem, testMoves);
	}

}
