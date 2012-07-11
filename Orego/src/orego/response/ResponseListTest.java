package orego.response;

/**
 * Tests for the ResponseList object.
 */

import static org.junit.Assert.*;

import orego.core.Coordinates;

import org.junit.Before;
import org.junit.Test;

public class ResponseListTest {

	private RawResponseList responseList;
	
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testWinsAndLosses() {
		responseList = new RawResponseList();
		assertEquals(RawResponseList.NORMAL_WINS_PRIOR, responseList.getWins()[Coordinates.at(0,0)]);
		assertEquals(RawResponseList.NORMAL_RUNS_PRIOR, responseList.getRuns()[Coordinates.at(0,0)]);
		assertEquals(RawResponseList.PASS_WINS_PRIOR, responseList.getWins(Coordinates.PASS));
		assertEquals(RawResponseList.PASS_RUNS_PRIOR, responseList.getRuns(Coordinates.PASS));
		responseList.addLoss(Coordinates.at(0,0));
		responseList.addWin(Coordinates.at(0,0));
		assertEquals(RawResponseList.NORMAL_WINS_PRIOR + 1, responseList.getWins()[Coordinates.at(0,0)]);
		assertEquals(RawResponseList.NORMAL_RUNS_PRIOR + 2, responseList.getRuns()[Coordinates.at(0,0)]);
		assertEquals(0.5,responseList.getWinRate(Coordinates.at(0, 0)),0.001);
	}

}

