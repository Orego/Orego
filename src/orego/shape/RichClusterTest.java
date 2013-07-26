package orego.shape;

import static orego.core.Coordinates.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import orego.core.*;

public class RichClusterTest {

	private Board board;
	
	private RichCluster cluster;
	
	@Before
	public void setUp() throws Exception {
		cluster = new RichCluster(4, 16);
		board = new Board();
	}

	@Test
	public void testHistory() {
		board.play("k1");
		board.play("k2");
		board.play("k3");
		board.play("k4");
		cluster.store(board, at("k5"), 0);
		board.play("e1");
		board.play("e2");
		board.play("e3");
		board.play("e4");
		cluster.store(board, at("e5"), 1);
		assertTrue(cluster.getWinRate(board, at("e5")) > cluster.getWinRate(board, at("k5")));
	}

	@Test
	public void testTransposition() {
		board.play("k1");
		board.play("k2");
		board.play("k3");
		board.play("k4");
		cluster.store(board, at("k5"), 0);
		board.play("e1");
		board.play("e2");
		board.play("e3");
		board.play("e4");
		cluster.store(board, at("e5"), 1);
		board.clear();
		board.play("k1");
		board.play("k2");
		board.play("k3");
		board.play("e2");
		board.play("e1");
		board.play("k4");
		board.play("e3");
		board.play("e4");
		assertTrue(cluster.getWinRate(board, at("e5")) > cluster.getWinRate(board, at("k5")));
	}

}
