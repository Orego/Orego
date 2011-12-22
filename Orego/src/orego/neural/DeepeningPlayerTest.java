package orego.neural;

import static org.junit.Assert.*;

import org.junit.*;

public class DeepeningPlayerTest {

	DeepeningPlayer player;

	@Before
	public void setUp() throws Exception {
		player = new DeepeningPlayer();
		player.setProperty("threads", "1");
		player.setPlayoutLimit(1001);
		player.reset();
	}

//	@Test
//	public void testCutoff() {
//		player.bestMove();
//		assertEquals(2, player.getCutoff());
//	}
//
//	@Test
//	public void testCutoffReset() {
//		player.bestMove();
//		player.setPlayoutLimit(1);
//		player.bestMove();
//		assertEquals(2, player.getCutoff());
//	}

}
