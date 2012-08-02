package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.BOARD_WIDTH;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;
import orego.core.Coordinates;

import org.junit.Before;
import org.junit.Test;

public class LessThanFiveLibertyHeuristicTest {

	private Board board;
	
	private LessThanFiveLibertyHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new LessThanFiveLibertyHeuristic(1);
	}

	@Test
	public void testEvaluate() {
		String[] problem = new String[] {
					"#......O####......#",//19
					"#......O####O.....#",//18
					"#......O####O.....#",//17
					"#.......OOOO......#",//16
					"..................#",//15
					"...................",//14
					"...................",//13
					".......OOOO........",//12
					".......OO#O........",//11
					"...................",//10
					"...................",//9
					"...................",//8
					"...................",//7
					".......OOO.........",//6
					".......O#..........",//5
					"...................",//4
					"#..................",//3
					"#.................#",//2
					"#.................#"//1
				  // ABCDEFGHJKLMNOPQRST
				};
		board.setUpProblem(WHITE, problem);
		heuristic.prepare(board, random);
		assertEquals(0, heuristic.evaluate(at("a15"), board));
		assertEquals(0, heuristic.evaluate(at("b18"), board));
		assertEquals(0, heuristic.evaluate(at("t14"), board));
		assertEquals(3, heuristic.evaluate(at("b1"), board));
		assertEquals(4, heuristic.evaluate(at("s1"), board));
		assertEquals(3, heuristic.evaluate(at("k5"), board));
		assertEquals(4, heuristic.evaluate(at("k10"), board));
		assertEquals(50, heuristic.evaluate(at("n19"), board));
		
		board.clear();
		board.setUpProblem(BLACK, problem);
		assertEquals(0, heuristic.evaluate(at("a15"), board));
		assertEquals(0, heuristic.evaluate(at("b18"), board));
		assertEquals(0, heuristic.evaluate(at("t14"), board));
		assertEquals(3, heuristic.evaluate(at("b1"), board));
		assertEquals(4, heuristic.evaluate(at("s1"), board));
		assertEquals(3, heuristic.evaluate(at("k5"), board));
		assertEquals(4, heuristic.evaluate(at("k10"), board));
		assertEquals(50, heuristic.evaluate(at("n19"), board));
		int count = 0;
		for(int p: Coordinates.ALL_POINTS_ON_BOARD){
			if(heuristic.evaluate(p, board) > 0 && board.isLegal(p)){
				count++;
			}
		}
		assertEquals(18, count);
	}

}
