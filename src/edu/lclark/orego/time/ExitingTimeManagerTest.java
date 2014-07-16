package edu.lclark.orego.time;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.PlayerBuilder;
import edu.lclark.orego.mcts.SearchNode;

public class ExitingTimeManagerTest {
	
	private Player player;
	
	private ExitingTimeManager manager;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		player = new PlayerBuilder().threads(1).timeManagement("exiting").openingBook(false).memorySize(64).build();
		coords = player.getBoard().getCoordinateSystem();
		manager = (ExitingTimeManager)player.getTimeManager();
	}

	@Test
	public void testCompareRest() {
		player.setRemainingTime(10000);
		SearchNode root = player.getRoot();
		assertNotEquals(0, manager.getMsec());
		root.update(coords.at("a5"), 1000, 1000);
		assertEquals(0, manager.getMsec(), .01);
	}
	
	@Test
	public void testRollover(){
		player.setRemainingTime(10000);
		SearchNode root = player.getRoot();
		assertNotEquals(0, manager.getMsec());
		root.update(coords.at("a5"), 1000, 1000);
		assertEquals(0, manager.getMsec(), .01);
		assertNotEquals(0, manager.getRollover());
		for(short p : coords.getAllPointsOnBoard()){
			if(p != coords.at("a5")){
				root.update(p, 1000, 1000);
			}
		}
		while(manager.getMsec() != 0){
			manager.getMsec();
		}
		assertEquals(0, manager.getRollover());
	}

}
