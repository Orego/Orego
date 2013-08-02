package orego.heuristic;

import static orego.core.Colors.*;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

public class LadderHeuristicTest {
	
	private Board board;
	
	private LadderHeuristic heuristic;
		
	@Before
	public void setUp() throws Exception {
		board = new Board();
		heuristic = new LadderHeuristic(1);
	}

	@Test
	public void testClone() throws Exception {
		LadderHeuristic copy = heuristic.clone();
		
		// should be a new instance
		assertFalse(copy == heuristic);
	}
	
	@Test
	public void testStoneInAtari() {		
		String[] diagram = {
				"...................",// 19
				"...................",// 18
				"...................",// 17
				"...................",// 16
				".....O.............",// 15
				"....O#O............",// 14
				"....O..............",// 13
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
		}; // length is 48
		
		board.setUpProblem(BLACK, diagram);
		heuristic.prepare(board);
		assertFalse(heuristic.getGoodMoves().contains(at("F13")));
		assertFalse(heuristic.getGoodMoves().contains(at("G15")));
	}
	
	@Test
	public void testInitialInAtari() {		
		String[] diagram = {
				"...................",// 19
				"...##..............",// 18
				"..#OOO##...........",// 17
				"..#O##O............",// 16
				"..O###O............",// 15
				"...OO#O............",// 14
				"....O#.............",// 13
				".....O.............",// 12
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
		System.err.println("testing");
		board.setUpProblem(BLACK, diagram);
		heuristic.prepare(board);
		System.err.println(heuristic.getGoodMoves());
		assertTrue(heuristic.getGoodMoves().contains(at("G13")));
	}



}
