package edu.lclark.orego.feature;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.mcts.CopiableStructureFactory;
import edu.lclark.orego.mcts.Player;
import edu.lclark.orego.mcts.SearchNode;
import edu.lclark.orego.mcts.SimpleSearchNodeBuilder;
import edu.lclark.orego.mcts.SimpleTreeUpdater;
import edu.lclark.orego.mcts.TranspositionTable;
import edu.lclark.orego.mcts.UctDescender;

public class ExitingTimeManagerTest {
	
	private Player player;
	
	private ExitingTimeManager manager;
	
	private CoordinateSystem coords;
	
	@Before
	public void setUp() throws Exception {
		final int milliseconds = 10000;
		final int threads = 1;
		player = new Player(threads, CopiableStructureFactory.useWithPriors(19, 7.5));
		coords = player.getBoard().getCoordinateSystem();
		Board board = player.getBoard();
		TranspositionTable table = new TranspositionTable(new SimpleSearchNodeBuilder(coords), coords);
		player.setTreeDescender(new UctDescender(board, table, 75));
		SimpleTreeUpdater updater = new SimpleTreeUpdater(board, table, 0);
		player.setTreeUpdater(updater);
		player.setMsecPerMove(milliseconds);
		manager = new ExitingTimeManager(player);
		player.setTimeManager(manager);
		player.clear();		
	}

	@Test
	public void testCompareRest() {
		player.setRemainingTime(10);
		SearchNode root = player.getRoot();
		assertNotEquals(0, manager.getTime());
		root.update(coords.at("a5"), 1000, 1000);
		assertEquals(0, manager.getTime(), .01);
		
	}
	
	@Test
	public void testRollover(){
		player.setRemainingTime(10);
		SearchNode root = player.getRoot();
		assertNotEquals(0, manager.getTime());
		
		root.update(coords.at("a5"), 1000, 1000);
		assertEquals(0, manager.getTime(), .01);
		assertNotEquals(0, manager.getRollover());
		for(short p : coords.getAllPointsOnBoard()){
			if(p != coords.at("a5")){
				root.update(p, 1000, 1000);
			}
		}
		while(manager.getTime() != 0){
			manager.getTime();
		}
		assertEquals(0, manager.getRollover());
	}

}
