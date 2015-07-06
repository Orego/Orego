package edu.lclark.orego.feature;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.StoneColor.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;

public class LgrfTableTest {
	
	private CoordinateSystem coords;
	
	private LgrfTable table;
	
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		coords = CoordinateSystem.forWidth(19);
		table = new LgrfTable(coords);
	}

	@Test
	public void testSet() {
		table.setReply(BLACK, at("a1"), at("b1"), at("c1"));
		table.setReply(BLACK, RESIGN, at("b1"), at("d1"));
		table.setReply(WHITE, at("a1"), at("b1"), at("e1"));
		table.setReply(WHITE, RESIGN, at("b1"), at("f1"));
		assertEquals(at("c1"), table.getSecondLevelReply(BLACK, at("a1"), at("b1")));
		assertEquals(at("d1"), table.getFirstLevelReply(BLACK, at("b1")));
		assertEquals(at("e1"), table.getSecondLevelReply(WHITE, at("a1"), at("b1")));
		assertEquals(at("f1"), table.getFirstLevelReply(WHITE, at("b1")));
	}
	
	@Test
	public void testUpdate() {
		table.update(BLACK, true, at("a1"), at("b1"), at("c1"));
		assertEquals(at("c1"), table.getFirstLevelReply(BLACK, at("b1")));
		assertEquals(NO_POINT, table.getFirstLevelReply(BLACK, at("a1")));
		assertEquals(at("c1"), table.getSecondLevelReply(BLACK, at("a1"), at("b1")));
	}
	
	@Test
	public void testClear(){
		table.update(BLACK, true, at("a1"), at("b1"), at("c1"));
		table.clear();
		assertEquals(NO_POINT, table.getFirstLevelReply(BLACK, at("b1")));
	}

}
