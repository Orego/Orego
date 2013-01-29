package orego.decision;

import static orego.core.Coordinates.at;

public class Experiment7 {

	public static void main(String[] args) {
		// LL 1001LDP/401, UL 402, UR 403
		String[] problem = { //
				"O.O##..O.", // 9
				"..O##OO..", // 8
				".O.###OOO", // 7
				"#O.#..###", // 6
				".###.##OO", // 5
				".####.#O.", // 4
				"#OOOO##O.", // 3
				".O.#O##O.", // 2                                                                                                            
				"#.#OO##O." // 1
		      // ABCDEFGHJ
		};
		int[] testMoves = {
				at("a2"), at("c2"),
				at("a8"), at("b8"), at("b9"), at("a7"),
				at("f9"), at("j8"),
				at("h3"), at("h2"),
				};
		new Experiment1().run(problem, testMoves);
	}

}
