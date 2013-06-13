package orego.heuristic;

import orego.core.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class HeuristicListTest {
	
	private HeuristicList heuristics;
	
	@Before
	public void setup() {
		heuristics = new HeuristicList();
	}
	
	@Test
	public void testPopulateHeuristics() {
		heuristics.loadHeuristics("Escape@5:Capture");
		assertEquals(2, heuristics.size());
		assertEquals(5, heuristics.get(0).getWeight());
		assertEquals(1, heuristics.get(1).getWeight());
		assertEquals(EscapeHeuristic.class, heuristics.get(0).getClass());
		assertEquals(CaptureHeuristic.class, heuristics.get(1).getClass());
		heuristics = new HeuristicList("Pattern@23:SpecificPoint@2");
		assertEquals(2, heuristics.size());
	}
	
	@Test
	public void testClone() {
		heuristics.loadHeuristics("Escape@2:Capture@3");
		HeuristicList cloned = heuristics.clone();
		
		// make sure not the same object (different copies)
		assertTrue(cloned != heuristics);
		
		assertEquals(2, cloned.size());
		
		// make sure not the same object (different copies)
		assertFalse(cloned.get(0) == heuristics.get(0));
		assertFalse(cloned.get(1) == heuristics.get(1));
		
		assertEquals(2, cloned.get(0).getWeight(), .000001);
		assertEquals(3, cloned.get(1).getWeight(), .000001);
		
		assertEquals(EscapeHeuristic.class, heuristics.get(0).getClass());
		assertEquals(CaptureHeuristic.class,   heuristics.get(1).getClass());
		
	}
		
	@Test
	public void testSetProperty() throws Exception {
		heuristics.loadHeuristics("Escape@2:Capture@3");
		assertEquals(2, heuristics.size());
		assertEquals(EscapeHeuristic.class,  heuristics.get(0).getClass());
		assertEquals(CaptureHeuristic.class, heuristics.get(1).getClass());
		heuristics.setProperty("heuristic.Escape.weight", "10");
		assertEquals(10, heuristics.get(0).getWeight(), .000001);
		heuristics.setProperty("heuristic.Capture.weight", "5");
		assertEquals(5.0, heuristics.get(1).getWeight(), .000001);
	}
	
	@Test
	public void testZeroWeightHeuristic() {
		heuristics.loadHeuristics("Escape@2:Capture@0:Pattern@10");
		assertEquals(2, heuristics.size());
	}

	@Test
	public void testSelectAndPlayOneMove() {
		heuristics.loadHeuristics("Capture@1:Pattern@1000");
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
				"....###............",//6
				"...#OOO............",//5
				"....###O...........",//4
				"...................",//3
				"...................",//2
				"..................."//1
		      // ABCDEFGHJKLMNOPQRST
		};
		Board board = new Board();
		board.setUpProblem(WHITE, problem);
		board.play(at("h6"));
		// The heuristics should choose the capture move, even though a pattern match would be more highly rated.
		assertEquals(at("h5"), heuristics.selectAndPlayOneMove(new MersenneTwisterFast(), board));	
	}

	@Test
	public void testSelectAndPlayOneMoveDistant() {
		heuristics.loadHeuristics("Capture@1:Pattern@1000");
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
				"....###O...........",//6
				"...#OOO............",//5
				"....###O...........",//4
				"...................",//3
				"...................",//2
				"..................."//1
		      // ABCDEFGHJKLMNOPQRST
		};
		Board board = new Board();
		board.setUpProblem(WHITE, problem);
		board.play(at("r16"));
		// The heuristics should choose the capture move, even though a pattern match would be more highly rated.
//		System.out.println(pointToString(heuristics.selectAndPlayOneMove(new MersenneTwisterFast(), board)));
		assertEquals(at("h5"), heuristics.selectAndPlayOneMove(new MersenneTwisterFast(), board));	
	}


	@Test
	public void testNoHeuristics() throws Exception {
		heuristics.setProperty("heuristics", "");
		assertEquals(0, heuristics.size());
	}

}
