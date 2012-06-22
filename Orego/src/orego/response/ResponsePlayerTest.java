package orego.response;

import static org.junit.Assert.*;

import orego.core.Coordinates;
import orego.response.ResponseList;

import org.junit.Before;
import org.junit.Test;

public class ResponsePlayerTest {
	
	ResponsePlayer player;

	@Before
	public void setUp() throws Exception {
		player = new ResponsePlayer();
	}

	@Test
	public void testResponseChoice() {
		
		//assertEquals(Coordinates.PASS, player.getHistoryInfo()[player.getHistoryInfo().length-2].getMove());
		//assertEquals(ResponseList.PASS_RUNS_BIAS, player.getHistoryInfo()[player.getHistoryInfo().length-2].getRuns());
		//assertEquals(null, player.getHistoryInfo()[Coordinates.NO_POINT].getHistoryInfo(Coordinates.NO_POINT).getHistoryInfo(Coordinates.NO_POINT).getHistoryInfo(Coordinates.NO_POINT));
	}

}
