package orego.cluster;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import static orego.core.Coordinates.*;

import java.rmi.registry.Registry;

import orego.cluster.RMIStartup.RegistryFactory;
import orego.mcts.Lgrf2Player;
import orego.play.Player;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

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
	private Player player;
	
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
		int[] wins = new int[4];
		int[] runs = new int[4];
		
		when(player.getWins()).thenReturn(wins);
		when(player.getPlayouts()).thenReturn(runs);
		
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
