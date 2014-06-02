package edu.lclark.orego.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortList;
import static edu.lclark.orego.core.StoneColor.*;

public class HistoryObserverTest {

	private HistoryObserver observer;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		observer = new HistoryObserver(new Board(5));
		coords = CoordinateSystem.forWidth(5);
	}

	@Test
	public void testUpdate() {
		ShortList none = new ShortList(0);
		observer.update(BLACK, coords.at("a2"), none);
		observer.update(WHITE, coords.at("b3"), none);
		observer.update(BLACK, coords.at("d1"), none);
		assertEquals(coords.at("a2"), observer.get(0));
		assertEquals(coords.at("b3"), observer.get(1));
		assertEquals(coords.at("d1"), observer.get(2));
	}

}
