package orego.cluster;

import static orego.cluster.ClusterPlayerTest.setupMockSearcher;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import orego.cluster.RMIStartup.RegistryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VotingClusterPlayerTest {

	@Mock private RegistryFactory mockFactory;
	
	@Mock private Registry mockRegistry;
	
	private VotingClusterPlayer player;
	
	@Before
	public void setUp() throws Exception {
		
		when(mockFactory.getRegistry()).thenReturn(mockRegistry);
		
		ClusterPlayer.factory = mockFactory;
		
		player = new VotingClusterPlayer();
		
		player.reset();
		
	}

	@Test
	public void testBreaksTiesCorrectly() throws RemoteException {
		int best = at("e4");
		
		// e4 and e5 will both have one vote, but e4 will have more wins.
		TreeSearcher searcherA = mock(TreeSearcher.class);
		when(searcherA.getSearcherId()).thenReturn(0);
		setupMockSearcher(searcherA, player, 100, best, 10, 10);
		player.addSearcher(searcherA);
		
		TreeSearcher searcherB = mock(TreeSearcher.class);
		when(searcherB.getSearcherId()).thenReturn(1);
		setupMockSearcher(searcherB, player, 100, at("e5"));
		player.addSearcher(searcherB);
		
		assertEquals(player.bestMove(), best);
	}

	@Test
	public void testWeightsByVotes() throws RemoteException {
		int best = at("e4");
		
		// e5 will have more wins (20).
		TreeSearcher searcherA = mock(TreeSearcher.class);
		when(searcherA.getSearcherId()).thenReturn(0);
		setupMockSearcher(searcherA, player, 100, at("e5"), 10, 10);
		player.addSearcher(searcherA);
		
		// But two searchers will vote for e4.
		TreeSearcher searcherB = mock(TreeSearcher.class);
		when(searcherB.getSearcherId()).thenReturn(1);
		setupMockSearcher(searcherB, player, 100, best);
		player.addSearcher(searcherB);
		
		TreeSearcher searcherC = mock(TreeSearcher.class);
		when(searcherC.getSearcherId()).thenReturn(2);
		setupMockSearcher(searcherC, player, 100, best);
		player.addSearcher(searcherC);
		
		// The voting player should choose the point with the most votes.
		assertEquals(player.bestMove(), best);

	}
	
	@Test
	public void testDoesNotChooseOccupiedPoints() throws RemoteException {
		int bestButOccupied = at("e4");
		
		TreeSearcher searcher = mock(TreeSearcher.class);
		when(searcher.getSearcherId()).thenReturn(1);
		setupMockSearcher(searcher, player, 100, bestButOccupied);
		player.addSearcher(searcher);
		
		player.acceptMove(at("e4"));
		
		assertTrue(player.bestMove() != bestButOccupied);
	}
	
}
