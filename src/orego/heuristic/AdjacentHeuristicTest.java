package orego.heuristic;

import org.junit.Before;
import org.junit.Test;
import orego.core.Board;
import static org.junit.Assert.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.at;

public class AdjacentHeuristicTest {

	private Board board;
	
	private AdjacentHeuristic heuristic;
	
	@Before
	public void setUp() throws Exception{
		board = new Board();
		heuristic = new AdjacentHeuristic(1);
	}
	
	@Test
	public void testClone() throws Exception {
		AdjacentHeuristic copy = heuristic.clone();
		assertFalse(copy == heuristic);
	}
	
	@Test
	public void testFindCoordinates(){
		board.play(at("c3"));
		board.play(at("s2"));
		heuristic.prepare(board);
		assertTrue(heuristic.getGoodMoves().contains(at("c4")));
		assertTrue(heuristic.getGoodMoves().contains(at("c2")));
		assertTrue(heuristic.getGoodMoves().contains(at("d3")));
		assertTrue(heuristic.getGoodMoves().contains(at("b3")));
		
	}
	/** Makes sure illegal moves aren't suggested */
	@Test
	public void testFindCoordinatesWithoutPieces(){
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
			".#.O...............",//3
			"..O................",//2
			"..................."//1
		  // ABCDEFGHJKLMNOPQRST	
		};
		board.setUpProblem(BLACK, problem);
		board.play(at("c3"));
		board.play(at("s2"));
		heuristic.prepare(board);
		assertTrue(heuristic.getGoodMoves().contains(at("c4")));
		assertEquals(heuristic.getGoodMoves().size(), 1);
	}
	
	/** Makes sure moves off the board aren't suggested */
	@Test
	public void testFindCoordinatesInCorners(){
		board.play(at("a1"));
		board.play(at("s2"));
		heuristic.prepare(board);
		assertTrue(heuristic.getGoodMoves().contains(at("a2")));
		assertTrue(heuristic.getGoodMoves().contains(at("b1")));
		assertEquals(heuristic.getGoodMoves().size(), 2);	
	}

}
