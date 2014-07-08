package edu.lclark.orego.time;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SimpleTimeManagerTest {

	private SimpleTimeManager manager;
	
	@Before
	public void setUp() throws Exception {
		manager = new SimpleTimeManager(123);
	}

	@Test
	public void testGetTime() {
		assertEquals(123, manager.getTime());
		assertEquals(0, manager.getTime());
		manager.startNewTurn();
		assertEquals(123, manager.getTime());
	}

}
