package orego.decision;

import static orego.core.Coordinates.*;

public class Experiment5 {

	public static void main(String[] args) {
		//UL GGP V4 180 BR GGP V3 72
		String[] problem = { //
				"....#O..O", // 9
				".O.##O.O.", // 8
				"..O##OO.O", // 7
				".OO##OOOO", // 6
				".O#.#####", // 5
				".########", // 4
				"##OOOOOO.", // 3
				"##OOOOO.O", // 2                                                                                                            
				".#####.O." // 1
		      // ABCDEFGHJ
		};
		int[] testMoves = {
				at("c8"), at("b9"), at("a5"), at("a7"),
				at("j3"), at("g1"),
				};
		new Experiment1().run(problem, testMoves);
	}

}
