package orego.book;

import org.junit.Before;
import org.junit.Test;
import static orego.core.Coordinates.*;
import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.core.Board;
import static org.junit.Assert.*;

public class FusekiBookTest {

	private Board board;
	private FusekiBook gen;
	private Player player;

	static {
		try {
			BookBuilder builder = new FusekiBookBuilder(2);
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
		gen = new FusekiBook("SgfTestFiles");
		board = new Board();
		board.clear();
		player.reset();
	}

	@Test
	public void testFusekiBook1() {
		String[] correct;
			correct = new String[] { "q4", "d16", "c4" };
		for (String move : correct) {
			assertEquals(pointToString(at(move)), pointToString(gen.nextMove(board)));
			int m = gen.nextMove(board);
			board.play(m);
		}
	}

	@Test
	public void testOpeningBook() throws UnknownPropertyException {
		player.setOpeningBook(gen);
		int p = player.bestMove();
			assertEquals(pointToString(at("q4")), pointToString(p));
	}

}
