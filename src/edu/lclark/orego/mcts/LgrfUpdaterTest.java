package edu.lclark.orego.mcts;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.feature.LgrfTable;
import static edu.lclark.orego.core.CoordinateSystem.NO_POINT;
import static edu.lclark.orego.core.StoneColor.*;

public class LgrfUpdaterTest {
	
	private Player player;
	
	private CoordinateSystem coords;
	
	private LgrfUpdater updater;
	
	private LgrfTable lgrfTable;

	@Before
	public void setUp() throws Exception {
		// TODO This is an awful lot of work, done (e.g.) here and in PlayoutSpeed. Encapsulate!
		final int milliseconds = 100;
		final int threads = 4;
		CopiableStructure copy = CopiableStructureFactory.lgrfWithPriors(5, 7.5);
		player = new Player(threads, copy);
		Board board = player.getBoard();
		coords = board.getCoordinateSystem();
		TranspositionTable table = new TranspositionTable(64, new SimpleSearchNodeBuilder(coords), coords);
		player.setTreeDescender(new UctDescender(board, table, 75));
		lgrfTable = copy.get(LgrfTable.class);
		updater = new LgrfUpdater(new SimpleTreeUpdater(board, table, 0), lgrfTable);
		player.setTreeUpdater(updater);
		player.setMsecPerMove(milliseconds);
	}
	
	@Test
	public void testLgrfUpdate(){
		McRunnable runnable = player.getMcRunnable(0);
		runnable.acceptMove(coords.at("a1"));
		runnable.acceptMove(coords.at("b1"));
		runnable.acceptMove(coords.at("c1"));
		updater.updateTree(BLACK, runnable);
		assertEquals(coords.at("c1"), lgrfTable.getFirstLevelReply(BLACK, coords.at("b1")));
		assertEquals(coords.at("a1"), lgrfTable.getFirstLevelReply(BLACK, NO_POINT));
		assertEquals(coords.at("a1"), lgrfTable.getSecondLevelReply(BLACK, NO_POINT, NO_POINT));
		assertEquals(NO_POINT, lgrfTable.getFirstLevelReply(BLACK, coords.at("a1")));
		assertEquals(coords.at("c1"), lgrfTable.getSecondLevelReply(BLACK, coords.at("a1"), coords.at("b1")));
	}

}
