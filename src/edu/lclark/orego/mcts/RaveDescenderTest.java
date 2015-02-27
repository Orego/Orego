package edu.lclark.orego.mcts;

import static org.junit.Assert.*;
import static edu.lclark.orego.core.StoneColor.*;

import org.junit.Before;
import org.junit.Test;

import edu.lclark.orego.core.CoordinateSystem;

public class RaveDescenderTest {
	
	Player player;
	
	McRunnable runnable;
	
	TranspositionTable table;
	
	RaveDescender descender;
	
	SimpleTreeUpdater updater;

	@Before
	public void setUp() throws Exception {
		player = new Player(1, CopiableStructureFactory.feasible(5));
		runnable = player.getMcRunnable(0);
		CoordinateSystem coords = player.getBoard().getCoordinateSystem();
		table = new TranspositionTable(100, new RaveNodeBuilder(coords), coords);
		descender = new RaveDescender(player.getBoard(), table, 0);
		updater = new SimpleTreeUpdater(player.getBoard(), table, 0);
		player.setTreeDescender(descender);
		player.setTreeUpdater(updater);
	}
	
	/** Delegate method to call at on coords. */
	private short at(String label) {
		return player.getBoard().getCoordinateSystem().at(label);
	}

	@Test
	public void testIncorporateRun() {
		player.acceptMove(at("a1"));
		RaveNode root = (RaveNode) updater.getRoot();
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(at("a2"));
		runnable.acceptMove(at("a3"));
		runnable.acceptMove(at("a4"));
		updater.updateTree(BLACK, runnable);
		assertEquals(0.333333, root.getWinRate(at("a2")), 0.01);
		runnable.copyDataFrom(player.getBoard());
		runnable.acceptMove(at("a4"));
		runnable.acceptMove(at("a3"));
		runnable.acceptMove(at("a2"));
		updater.updateTree(BLACK, runnable);
		assertEquals(0.25, root.getRaveWinRate(at("a2")), 0.01);
		assertEquals(0.2856f, descender.searchValue(root, at("a2")), 0.01f);
	}

	@Test
	public void testExcludedMove() {
		RaveNode root = (RaveNode) updater.getRoot();
		root.exclude(at("a1"));
		assertEquals(Double.NEGATIVE_INFINITY, descender.searchValue(root, at("a1")), 0.001);
	}

}
