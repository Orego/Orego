package orego.heuristic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

public class HeuristicSetTest {
	private HeuristicList heuristics;
	
	@Before
	public void setup() {
		heuristics = new HeuristicList();
	}
	
	@Test
	public void testPopulateHeuristics() {
		heuristics.loadHeuristicList("Escape@5:Capture@10");
		assertEquals(2, heuristics.size());
		assertEquals(5, heuristics.getHeuristics()[0].getWeight(), .000001);
		assertEquals(10, heuristics.getHeuristics()[1].getWeight(), .000001);
		assertEquals(EscapeHeuristic.class, heuristics.getHeuristics()[0].getClass());
		assertEquals(CaptureHeuristic.class, heuristics.getHeuristics()[1].getClass());
		heuristics = new HeuristicList("Territory@23:LinesOneTwo@2");
		assertEquals(2, heuristics.size());
		assertEquals(TerritoryHeuristic.class, heuristics.getHeuristics()[0].getClass());
		assertEquals(LinesOneTwoHeuristic.class,   heuristics.getHeuristics()[1].getClass());
		assertEquals(23, heuristics.getHeuristics()[0].getWeight(), .000001);
		assertEquals(2, heuristics.getHeuristics()[1].getWeight(), .000001);
		// test default to 1 weighting
		heuristics = new HeuristicList("Territory:LinesOneTwo");
		assertEquals(TerritoryHeuristic.class, 	   heuristics.getHeuristics()[0].getClass());
		assertEquals(LinesOneTwoHeuristic.class,   heuristics.getHeuristics()[1].getClass());
		assertEquals(2, heuristics.size());
		assertEquals(1, heuristics.getHeuristics()[0].getWeight(), .000001);
		assertEquals(1, heuristics.getHeuristics()[1].getWeight(), .000001);
	}
	
	@Test
	public void testClone() {
		heuristics.loadHeuristicList("Escape@2:Capture@3");
		HeuristicList cloned = heuristics.clone();
		assertEquals(2, cloned.size());
		assertEquals(2, cloned.getHeuristics()[0].getWeight(), .000001);
		assertEquals(3, cloned.getHeuristics()[1].getWeight(), .000001);
		assertEquals(EscapeHeuristic.class, heuristics.getHeuristics()[0].getClass());
		assertEquals(CaptureHeuristic.class,   heuristics.getHeuristics()[1].getClass());
		assertFalse(heuristics.getHeuristics()[0] == cloned.getHeuristics()[0]);
		assertFalse(heuristics.getHeuristics()[1] == cloned.getHeuristics()[1]);
	}
	
	@Test
	public void testBestMove() {
		heuristics.loadHeuristicList("Escape@2:Capture@3");
		// TODO: test the best move with actual moves
	}
	
	@Test
	public void testSetProperty() throws Exception {
		heuristics.loadHeuristicList("Escape@2:Capture@3");
		assertEquals(2, heuristics.size());
		assertEquals(EscapeHeuristic.class,  heuristics.getHeuristics()[0].getClass());
		assertEquals(CaptureHeuristic.class, heuristics.getHeuristics()[1].getClass());
		heuristics.setProperty("heuristic.Escape.weight", "10");
		assertEquals(10, heuristics.getHeuristics()[0].getWeight(), .000001);
		heuristics.setProperty("heuristic.Capture.weight", "5");
		assertEquals(5.0, heuristics.getHeuristics()[1].getWeight(), .000001);
		heuristics.setProperty("heuristics", "Territory@4:Pattern@5");
		assertEquals(TerritoryHeuristic.class, heuristics.getHeuristics()[0].getClass());
		assertEquals(PatternHeuristic.class,   heuristics.getHeuristics()[1].getClass());
		assertEquals(4, heuristics.getHeuristics()[0].getWeight(), .000001);
		assertEquals(5, heuristics.getHeuristics()[1].getWeight(), .000001);
	}
	
	@Test
	public void testZeroWeightHeuristic() {
		heuristics.loadHeuristicList("Escape@2:Capture@0:Pattern@10");
		assertEquals(2, heuristics.getHeuristics().length);
	}

}