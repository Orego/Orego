package orego.response;

/**
 * Tests for the History Info object.
 */

import static org.junit.Assert.*;

import orego.core.Coordinates;

import org.junit.Before;
import org.junit.Test;

public class HistoryInfoTest {

	private HistoryInfo historyInfo;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testWinsAndLosses() {
		historyInfo = new HistoryInfo(Coordinates.PASS);
		historyInfo.addWin();
		assertEquals(1, historyInfo.getWins());
		assertEquals(1, historyInfo.getRuns());
		historyInfo.addLoss();
		assertEquals(1, historyInfo.getWins());
		assertEquals(2, historyInfo.getRuns());
	}
	
	@Test
	public void testHistorySetup() {
		historyInfo = new HistoryInfo(Coordinates.PASS);
		historyInfo.setupHistory();
		for (int p : Coordinates.ALL_POINTS_ON_BOARD) {
			assertEquals(p, historyInfo.getHistoryInfo(p).getMove());
		}
		assertEquals(Coordinates.PASS, historyInfo.getHistoryInfo(Coordinates.PASS).getMove());
		assertEquals(Coordinates.NO_POINT, historyInfo.getHistoryInfo(Coordinates.NO_POINT).getMove());
	}

}
