package orego.book;

import org.junit.Before;
import org.junit.Test;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.WHITE;
import static orego.core.Coordinates.*;
import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.core.Board;
import static org.junit.Assert.*;

public class JosekiBookTest {

	private Board board;
	private JosekiBook gen;
	private Player player;

	static {
		try {
			JosekiBookBuilder builder = new JosekiBookBuilder(2);
			builder.buildRawBook("SgfTestFiles");
			builder.buildFinalBook("SgfTestFiles");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Before
	public void setUp() throws Exception {

		player = new Player();
		gen = new JosekiBook("SgfTestFiles");
		board = new Board();
		board.clear();
		player.reset();
	}

	@Test
	public void JosekiTest1() {
		String[] correct;
		correct = new String[] { "d16", "c14", "q16", "r14", "q4" };
		for (String move : correct) {
			int played = gen.nextMove(board);
			assertEquals(pointToString(at(move)), pointToString(played));
			board.play(played);
		}
	}

	@Test
	public void testOpeningBook() throws UnknownPropertyException {

		player.setOpeningBook(gen);
		player.reset();
		int p = player.bestMove();
		assertEquals("D16", pointToString(p));
	}

	@Test
	public void testOpeningBook2() {

		player.setOpeningBook(gen);
		player.reset();
		player.acceptMove(at("k10"));
		player.acceptMove(at("q4"));
		player.acceptMove(at("q10"));
		player.acceptMove(at("m3"));
		player.acceptMove(at("m10"));
		assertEquals(pointToString(at("d16")), pointToString(player.bestMove()));
	}

	@Test
	public void testBuildJosekiBoard() {

		board.play(at("c17"));
		board.play(at("s10"));
		board.play(at("c18"));
		board.play(at("d17"));
		board.play(at("a4"));
		board.play(at("s16"));
		board.play(at("c16"));
		board.play(at("q9"));
		board.play(at("c13"));
		board.play(at("b3"));
		board.play(at("b4"));
		board.play(at("d6"));
		board.play(at("g8"));

		String[] answer = { "...................",// 19
				"..#................",// 18
				"..#O...............",// 17
				"..#................",// 16
				"...................",// 15
				"...................",// 14
				"..#................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
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
		Board answerBoard = new Board();
		answerBoard.setUpProblem(WHITE, answer);
		Board josekiBoard = new Board();
		josekiBoard = gen.createJosekiBoard(board, 0);
		assertEquals(answerBoard.getHash(), josekiBoard.getHash());
	}

	@Test
	public void testBuildJosekiBoard2() {

		board.play(at("c17"));
		board.play(at("r18"));
		board.play(at("q18"));
		board.play(at("b3"));
		board.play(at("r17"));
		board.play(at("s16"));
		board.play(at("r16"));
		board.play(at("c16"));
		board.play(at("d6"));
		board.play(at("b4"));
		board.play(at("q9"));
		board.play(at("c13"));
		board.play(at("g8"));

		String[] answer = { "...................",// 19
				"...............#O..",// 18
				"................#..",// 17
				"................#O.",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
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
		Board answerBoard = new Board();
		answerBoard.setUpProblem(WHITE, answer);
		Board josekiBoard = new Board();
		josekiBoard = gen.createJosekiBoard(board, 1);
		assertEquals(answerBoard.getHash(), josekiBoard.getHash());
	}

	@Test
	public void testBuildJosekiBoard3() {

		board.play(at("s7"));
		board.play(at("b4"));
		board.play(at("r7"));
		board.play(at("c16"));
		board.play(at("s6"));
		board.play(at("q18"));
		board.play(at("s5"));
		board.play(at("r6"));
		board.play(at("c13"));
		board.play(at("d6"));
		board.play(at("g8"));
		board.play(at("n4"));

		String[] answer = { "...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"...................",// 8
				"................##.",// 7
				"................O#.",// 6
				".................#.",// 5
				"............O......",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		Board answerBoard = new Board();
		answerBoard.setUpProblem(BLACK, answer);
		Board josekiBoard = new Board();
		josekiBoard = gen.createJosekiBoard(board, 2);
		assertEquals(answerBoard.getHash(), josekiBoard.getHash());
	}

	@Test
	public void testBuildJosekiBoard4() {

		board.play(at("s7"));
		board.play(at("b4"));
		board.play(at("b5"));
		board.play(at("c16"));
		board.play(at("c6"));
		board.play(at("q18"));
		board.play(at("s5"));
		board.play(at("r6"));
		board.play(at("c13"));
		board.play(at("d6"));
		board.play(at("g8"));
		board.play(at("n4"));
		board.play(at("r16"));

		String[] answer = { "...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				"...................",// 15
				"...................",// 14
				"...................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
				"...................",// 9
				"......#............",// 8
				"...................",// 7
				"..#O...............",// 6
				".#.................",// 5
				".O.................",// 4
				"...................",// 3
				"...................",// 2
				"..................."// 1
		// ABCDEFGHJKLMNOPQRST
		};
		Board answerBoard = new Board();
		answerBoard.setUpProblem(WHITE, answer);
		Board josekiBoard = new Board();
		josekiBoard = gen.createJosekiBoard(board, 3);
		assertEquals(answerBoard.getHash(), josekiBoard.getHash());
	}

	@Test
	public void testBuildJosekiBoardWithBothColors() {

		board.pass();
		board.play(at("c17"));
		board.play(at("s10"));
		board.play(at("c18"));
		board.play(at("d17"));
		board.play(at("a4"));
		board.play(at("s16"));
		board.play(at("c16"));
		board.play(at("q9"));
		board.play(at("c13"));
		board.play(at("b3"));
		board.play(at("b4"));
		board.play(at("d6"));
		board.play(at("g8"));
		String[] answer = { "...................",// 19
				"..O................",// 18
				"..O#...............",// 17
				"..O................",// 16
				"...................",// 15
				"...................",// 14
				"..O................",// 13
				"...................",// 12
				"...................",// 11
				"...................",// 10
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
		Board answerBoard = new Board();
		answerBoard.setUpProblem(BLACK, answer);
		Board josekiBoard = gen.createJosekiBoard(board, 0);
		assertEquals(answerBoard.getHash(), josekiBoard.getHash());
	}
}
