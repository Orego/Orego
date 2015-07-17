package edu.lclark.orego.genetic;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;
import org.junit.Before;
import org.junit.Test;

public class PlayerTest {

	private Player player;
	
	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().populationSize(100).individualLength(500).build();
	}

	@Test
	public void testCreatePopulations() {
		assertEquals(100, player.getPopulations()[BLACK.index()].size());
	}

}
