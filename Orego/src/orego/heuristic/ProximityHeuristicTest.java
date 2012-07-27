package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class ProximityHeuristicTest {

	private Board board;
	
	private ProximityHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new ProximityHeuristic(1);
	}

	@Test
	public void testEvaluate() {
		String[] problem = new String[] {
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
		System.out.println(board.getTurn());
		board.play("j10");
		System.out.println(board.getTurn());
		System.out.println(board);
		assertEquals(1, heuristic.evaluate(at("h13"), board));
		assertEquals(1, heuristic.evaluate(at("j13"), board));
		assertEquals(1, heuristic.evaluate(at("k13"), board));
		assertEquals(1, heuristic.evaluate(at("g12"), board));
		assertEquals(1, heuristic.evaluate(at("h12"), board));
		assertEquals(1, heuristic.evaluate(at("j12"), board));
		assertEquals(1, heuristic.evaluate(at("k12"), board));
		assertEquals(1, heuristic.evaluate(at("l12"), board));
		assertEquals(1, heuristic.evaluate(at("k7"), board));
		assertEquals(1, heuristic.evaluate(at("l8"), board));
		assertEquals(0, heuristic.evaluate(at("n10"), board));
		assertEquals(0, heuristic.evaluate(at("k14"), board));
		assertEquals(0, heuristic.evaluate(at("f8"), board));



	}

}
