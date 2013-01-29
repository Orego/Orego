package orego.decision;

import static orego.core.Coordinates.*;

public class Experiment6 {

	public static void main(String[] args) {
		//UL GGP V4 149 BR GGP V3 72
		String[] problem = { //
				"..O.#O..O", // 9
				"...O#O.O.", // 8
				".OO.#OO.O", // 7
				".O###OOOO", // 6
				".##.#####", // 5
				"#########", // 4
				"##OOOOOO.", // 3
				"##OOOOO.O", // 2                                                                                                            
				".#####.O." // 1
		      // ABCDEFGHJ
		};
		int[] testMoves = {
				at("d7"), at("a8"), at("a6"), at("b9"),
				at("j3"), at("g1"),
				};
		new Experiment1().run(problem, testMoves);
	}

}
