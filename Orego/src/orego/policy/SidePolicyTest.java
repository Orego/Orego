package orego.policy;

import static orego.core.Colors.BLACK;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class SidePolicyTest {

	private Board board;

	private CapturePolicy policy;

	private MersenneTwisterFast random;

	@Before
	public void setUp() throws Exception {
		policy = new CapturePolicy();
		board = new Board();
		random = new MersenneTwisterFast();
	}

	@Test
	public void testPlayOnSide() {
				String[] problem = {
						"...................",// 19
						"O##................",// 18
						"O#.##.........#.....",// 17
						"O##.#..............",// 16
						"O###.##############",// 15
						"OO###.#############",// 14
						"OO####.##.#########",// 13
						"OO#####.########...",// 12
						"OOO#####.##########",// 11
						"OOO######.####.....",// 10
						"OOO#######.##..##OO",// 9
						"OO#########.##..#O#",// 8
						"OO##########.###OO.",// 7
						"OOOOO########.#####",// 6
						"OOOOOOO########.###",// 5
						"OOOOOOOOOOOO#OO####",// 4
						"OOOOOOOOOOOOOOOOOO#",// 3
						"OOOOOOOOOOOOOOOOOOO",// 2
						"OOOOOOOOOOOOOOO.OOO"// 1
						// ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);		
		
	}
}
