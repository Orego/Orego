package orego.response;

/**
 * Tests for the History Info object.
 */

import static org.junit.Assert.*;

import orego.core.Coordinates;

import org.junit.Before;
import org.junit.Test;

public class ResponseListTest {

	private ResponseList responseList;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testWinsAndLosses() {
		responseList = new ResponseList();
		assertEquals(ResponseList.NORMAL_WINS_BIAS, responseList.getWins()[responseList.getIndices()[Coordinates.at(0,0)]]);
		assertEquals(ResponseList.NORMAL_RUNS_BIAS, responseList.getRuns()[responseList.getIndices()[Coordinates.at(0,0)]]);
		assertEquals(ResponseList.PASS_WINS_BIAS, responseList.getWins()[responseList.getIndices()[Coordinates.PASS]]);
		assertEquals(ResponseList.PASS_RUNS_BIAS, responseList.getRuns()[responseList.getIndices()[Coordinates.PASS]]);
		responseList.addLoss(Coordinates.at(0,0));
		responseList.addWin(Coordinates.at(0,0));
		assertEquals(ResponseList.NORMAL_WINS_BIAS+1, responseList.getWins()[responseList.getIndices()[Coordinates.at(0,0)]]);
		assertEquals(ResponseList.NORMAL_RUNS_BIAS+2, responseList.getRuns()[responseList.getIndices()[Coordinates.at(0,0)]]);
		assertEquals(Coordinates.PASS, responseList.getMoves()[responseList.getIndices()[Coordinates.PASS]]);
		assertEquals(0.5,responseList.getWinRate(responseList.getIndices()[Coordinates.at(0, 0)]),0.001);
	}

}

