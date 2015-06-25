package edu.lclark.orego.neural;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.HistoryObserver;

public class DirectNetworkTest {

	private Board board;
	
	private DirectNetwork net;
	
	private CoordinateSystem coords;
	
	/** Delegate method to call at on coords. */
	private short at(String label) {
		return coords.at(label);
	}

	@Before
	public void setUp() throws Exception {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		net = DirectNetwork.readFromDisk(board, new HistoryObserver(board));
	}

	@Test
	public void testDirectNetwork() {
		net.update();
		assertTrue(net.getOutputActivation(at("d4")) > net.getOutputActivation(at("a1")));
	}

}
