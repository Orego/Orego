package orego.book;

import org.junit.Before;
import static org.junit.Assert.*;
import orego.play.Player;
import orego.core.Board;
import orego.core.Colors;
import static orego.core.Coordinates.*;

import org.junit.Test;

public class LateOpeningBookTest {

	protected Board board;
	protected LateOpeningBook gen;
	protected Player player;
	
	@Before
	public void setUp() throws Exception {
		player = new Player();
		gen = new LateOpeningBook();
		board = new Board();
		board.clear();
		player.reset();
	}
	
	@Test
	public void testContainedFusekiBook() {
		String[] correct;
			correct = new String[] { "q4", "d16", "c4" };
		for (String move : correct) {
			assertEquals(pointToString(at(move)), pointToString(gen.nextMove(board)));
			int m = gen.nextMove(board);
			board.play(m);
		}
	}
	
	@Test
	public void testPlayAtEmptyCorners() {
		board.play(at("o5"));
		int m = gen.nextMove(board);
		assertTrue(m == at("c3"));
		board.play(m);
		board.play(at("s5"));
		board.play(at("p15"));
		m = gen.nextMove(board);
		assertEquals(pointToString(at("c17")), pointToString(m));
		board.play(m);
	}
	
	@Test
	public void testPlayInWideOpenSpace() {
		String[] problem = new String[] {
				"...#...............",// 19
				"..........#........",// 18
				"................#..",// 17
				"...................",// 16
				"...................",// 15
				".....#.............",// 14
				"...................",// 13
				".#...........#.....",// 12
				"...................",// 11
				"...................",// 10
				"...............#...",// 9
				"...................",// 8
				"...................",// 7
				".................#.",// 6
				"...#...........#...",// 5
				"...................",// 4
				"..........#........",// 3
				"...................",// 2
				"...#..............." // 1
			  // ABCDEFGHJKLMNOPQRST
		};
		player.setUpProblem(Colors.WHITE, problem);
		int m = gen.nextMove(player.getBoard());
		assertEquals(pointToString(at("r13")), pointToString(m));
	}
	
	@Test
	public void testNoSuggestedMoves() {
		String[] problem = new String[] {
				"...#...............",// 19
				"..........#........",// 18
				"................#..",// 17
				"...................",// 16
				"...................",// 15
				".....#..........#..",// 14
				"...................",// 13
				".#...........#.....",// 12
				"...................",// 11
				"...................",// 10
				"...............#...",// 9
				"...................",// 8
				"...................",// 7
				".................#.",// 6
				"...#...........#...",// 5
				"...................",// 4
				"..........#........",// 3
				"...................",// 2
				"..................." // 1
			  // ABCDEFGHJKLMNOPQRST
		};
		player.setUpProblem(Colors.WHITE, problem);
		int m = gen.nextMove(player.getBoard());
		assertEquals(pointToString(NO_POINT), pointToString(m));
	}

}
