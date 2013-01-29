package orego.book;

import static orego.core.Colors.*;
import static org.junit.Assert.*;
import orego.core.Board;
import orego.core.Coordinates;
import orego.ui.Orego;
import org.junit.Before;
import org.junit.Test;


public class StarPointsBookTest {

	private StarPointsBook gen;
	private Board board;
	
	@Before
	public void setUp() throws Exception {
		gen = new StarPointsBook();
		board = new Board();
	}
	
	@Test
	public void testOpeningBook() {
			for(int j = 0; j < 9; j++) {
				String[] problem = {
						"...................",//19
						"...................",//18
						"...................",//17
						"...................",//16
						"...................",//15
						"...................",//14
						"...................",//13
						"...................",//12
						"...................",//11
						"...................",//10
						"...................",//9
						"...................",//8
						"...................",//7
						"...................",//6
						"...................",//5
						"...................",//4
						"...................",//3
						"...................",//2
						"..................."//1
					  // ABCDEFGHJKLMNOPQRST
				};
				board.setUpProblem(WHITE, problem);
				int move = gen.nextMove(board);
				assertTrue((Coordinates.at("d4") == move)
						|| (Coordinates.at("d16") == move)
						|| (Coordinates.at("q4") == move)
						|| (Coordinates.at("q16") == move)
						|| (Coordinates.at("k16") == move)
						|| (Coordinates.at("k4") == move)
						|| (Coordinates.at("d10") == move)
						|| (Coordinates.at("q10") == move)
						|| (Coordinates.at("k10") == move));
			}
	}
	
	@Test
	public void testOpeningBookSetup() {
		String[] args = { "book=StarPointsBook" };
		Orego orego = new Orego(System.in, System.out, args);
			for(int j = 0; j < 9; j++) {
				String[] problem = {
						"...................",//19
						"...................",//18
						"...................",//17
						"...................",//16
						"...................",//15
						"...................",//14
						"...................",//13
						"...................",//12
						"...................",//11
						"...................",//10
						"...................",//9
						"...................",//8
						"...................",//7
						"...................",//6
						"...................",//5
						"...................",//4
						"...................",//3
						"...................",//2
						"..................."//1
					  // ABCDEFGHJKLMNOPQRST
				};
				orego.getPlayer().getBoard().setUpProblem(BLACK, problem);
				int move = orego.getPlayer().bestMove();
				assertTrue((Coordinates.at("d4") == move)
						|| (Coordinates.at("d16") == move)
						|| (Coordinates.at("q4") == move)
						|| (Coordinates.at("q16") == move)
						|| (Coordinates.at("k16") == move)
						|| (Coordinates.at("k4") == move)
						|| (Coordinates.at("d10") == move)
						|| (Coordinates.at("q10") == move)
						|| (Coordinates.at("k10") == move));
		}

	}

}
