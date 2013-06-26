package orego.core;

import static orego.core.Colors.WHITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class TerritoryEstimatorTest {

	@Test
	public void testSimpleDilation() {
		Board board = new Board();
		String[] problem = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				".......#..#........",// 10
				"...................",// 9
				"...................",// 8
				"...................",// 7
				"...................",// 6
				"...................",// 5
				"...................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		TerritoryEstimator inf = new TerritoryEstimator(board);
		System.out.println(inf);
		inf.dilateMultipleTimes(3);
		System.out.println(inf);
		inf.erodeMultipleTimes(1);
		System.out.println(inf);
		inf.erodeMultipleTimes(6);
		System.out.println(inf);
	}
	
	@Test
	public void testX() {
		Board board = new Board();
		String[] problem = {
				"...................",// 19
				"..OO#..............",// 18
				"..O#.#...#.O.O..O..",// 17
				"..O#...........O.#.",// 16
				".O#......#.........",// 15
				".O#.............#..",// 14
				"..#................",// 13
				"...............#...",// 12
				"...................",// 11
				"..O.............#..",// 10
				"...................",// 9
				"...................",// 8
				"..O.............O..",// 7
				"...................",// 6
				"....#...........O..",// 5
				"..#...........OO##.",// 4
				"....#.O..O....O#...",// 3
				".............O##...",// 2
				"..................."// 1
		      // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(WHITE, problem);
		TerritoryEstimator inf = new TerritoryEstimator(board);
		System.out.println(inf);
		inf.dilateMultipleTimes(5);
		inf.erodeMultipleTimes(13);
		System.out.println(inf);
	}
}
