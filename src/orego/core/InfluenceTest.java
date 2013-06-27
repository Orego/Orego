package orego.core;

import static orego.core.Colors.WHITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class InfluenceTest {

//	@Test
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
		Influence inf = new Influence(board);
		System.out.println(inf);
		inf.bouzyDilate(3);
		System.out.println(inf);
		inf.bouzyErode(1);
		System.out.println(inf);
		inf.bouzyErode(6);
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
		Influence inf = new Influence(board);
		System.out.println(inf);

		inf.bouzyDilate(4);
		inf.bouzyErode(13);
		System.out.println(inf);
		
		Influence inf2 = new Influence(board);
		inf2.zobristDilate(4);
		System.out.println(inf2);
	}
}
