package orego.mcts;

import static org.junit.Assert.*;
import java.io.PipedOutputStream;
import orego.play.UnknownPropertyException;
import orego.policy.*;
import orego.ui.Orego;
import org.junit.*;

public class McPlayerTest {

	private McPlayer player;

	@Before
	public void setUp() throws Exception {
		player = new MctsPlayer();
		player.reset();
	}

	@Test
	public void testSetMillisecondsPerMove() {
		player.setMillisecondsPerMove(314);
		assertEquals(314, player.getMillisecondsPerMove());
		assertEquals(-1, player.getPlayoutLimit());
	}

	@Test
	public void testSetPlayoutLimit() {
		player.setPlayoutLimit(1000);
		assertEquals(1000, player.getPlayoutLimit());
		assertEquals(-1, player.getMillisecondsPerMove());
	}

	@Test
	public void testSetPropertyPlayouts() throws UnknownPropertyException {
		player.setProperty("playouts", "1000");
		assertEquals(player.getPlayoutLimit(), 1000);
		assertEquals(-1, player.getMillisecondsPerMove());
	}

	@Test
	/** This test is necessary because of the overridden setMillisecondsPerMove(). */
	public void testSetPropertyMillisecondsPerMove()
			throws UnknownPropertyException {
		player.setProperty("msec", "314");
		assertEquals(314, player.getMillisecondsPerMove());
		assertEquals(-1, player.getPlayoutLimit());
	}

	@Test
	public void testSetPropertyPolicy() throws UnknownPropertyException {
		player.setProperty("policy", "CapturePolicy:RandomPolicy");
		Policy gen = player.getPolicy();
		assertEquals(gen.getClass(), CapturePolicy.class);
	}

	@Test
	public void testMcCommandsAvaliable() {
		String[] args = { "player=MctsPlayer" };
		Orego orego = new Orego(System.in, new PipedOutputStream(), args);
		assertTrue(orego.getCommands().contains("gogui-mc-playouts"));
		assertTrue(orego.getCommands().contains("gogui-live-mc-playouts"));
		assertTrue(orego.getCommands().contains("gogui-win-rates"));
	}

}
