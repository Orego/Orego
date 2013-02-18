package orego.cluster;

import static orego.core.Coordinates.at;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.registry.Registry;

import orego.cluster.RMIStartup.RegistryFactory;
import orego.mcts.Lgrf2Player;
import orego.mcts.StatisticalPlayer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClusterTreeSearcherTest {

	private ClusterTreeSearcher searcher;
	
	@Mock
	private RegistryFactory factory;
	
	@Mock
	private Registry registry;
	
	@Mock
	private SearchController controller;
	
	@Mock
	private StatisticalPlayer player;
	
	@Before
	public void setup() throws Exception {
		when(factory.getRegistry()).thenReturn(registry);
		
		ClusterPlayer.factory = factory;
		
		// now we setup the mock cluster player
		doNothing().when(controller).addSearcher(any(TreeSearcher.class));
				
		searcher = new ClusterTreeSearcher(controller);
		
		// make certain we add ourselves
		verify(controller).addSearcher(searcher);

		searcher.setPlayer(player);
		
		// now we make certain to capture playout results
		// TODO: if I had more time, I would like to check the null edge case guards in the methods
	}
	
	@Test
	public void testShouldResetOnPlayer() throws Exception {
		searcher.reset();
		
		verify(player).reset();
	}
	
	@Test
	public void testShouldGetBestMoveFromPlayer() throws Exception {
		long[] wins = new long[4];
		long[] runs = new long[4];
		
		when(player.getBoardWins()).thenReturn(wins);
		when(player.getBoardPlayouts()).thenReturn(runs);
		
		when(player.bestMove()).thenReturn(at("a8"));
		
		searcher.beginSearch();
		
		// wait a bit for the background thread to work
		Thread.sleep(100);
		
		// did we call back to the controller?
		verify(controller).acceptResults(searcher, runs, wins);
	}
	
	@Test
	public void testShouldAddItselfToClusterPlayer() throws Exception {
		verify(controller).addSearcher(searcher);
	}
	
	@Test
	public void testShouldInitializeProperSubclassUsingReflection() {
		searcher.setPlayer(Lgrf2Player.class.getName());
		
		assertNotNull(searcher.getPlayer());
		assertTrue(searcher.getPlayer() instanceof Lgrf2Player);
	}
	
	@Test
	public void testShouldSetKomi() throws Exception {
		searcher.setKomi(5.0);
		
		verify(player).setKomi(5.0);
	}
	
	@Test
	public void testShouldSetPropertyOnPlayer() throws Exception {
		String key = "ponder";
		String value = "true";
		
		searcher.setProperty(key, value);
		
		verify(player).setProperty(key, value);
	}
	
	@Test
	public void testShouldAcceptMoveOnPlayer() throws Exception {
		searcher.acceptMove(0, at("a2"));
		
		verify(player).acceptMove(at("a2"));
	}

}
