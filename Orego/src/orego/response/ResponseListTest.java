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
		assertEquals(0.5,responseList.getWinRate(Coordinates.at(0, 0)),0.001);
	}
	
	@Test
	public void testSort() {
		responseList = new ResponseList();
		short[] moves = {2,3,1,5,0,4};
		responseList.setMoves(moves);
		short[] indices = {4,2,0,1,5,3};
		responseList.setIndices(indices);
		// start with unsorted win list
		short[] wins = {8,7,5,5,2,6};
		responseList.setWins(wins);
		short[] runs = {10,10,10,10,10,10};
		responseList.setRuns(runs);
		
		responseList.sort(4,1);
		// test sortWin
		assertEquals(6, responseList.getWins()[2]);
		assertEquals(4, responseList.getMoves()[2]);
		assertEquals(2, responseList.getIndices()[4]);
		
		// test sortLoss
		short[] newWins = {8,2,5,5,3,1};
		responseList.setWins(newWins);
		responseList.sort(3, -1);
		assertEquals(2, responseList.getWins()[4]);
		assertEquals(3, responseList.getMoves()[4]);
		assertEquals(4, responseList.getIndices()[3]);
	}

}

