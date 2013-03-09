package orego.cluster;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static orego.cluster.ClusterPlayerTest.setupMockSearcher;
import static orego.core.Coordinates.at;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import orego.cluster.RMIStartup.RegistryFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MaxClusterPlayerTest {

	@Mock private RegistryFactory mockFactory;
	
	@Mock private Registry mockRegistry;
	
	private MaxClusterPlayer player;
	
	@Before
	public void setUp() throws Exception {
		
		when(mockFactory.getRegistry()).thenReturn(mockRegistry);
		
		ClusterPlayer.factory = mockFactory;
		
		player = new MaxClusterPlayer();
		
		player.reset();
		
	}

	@Test
	public void testChoosesMaxPoint() throws RemoteException {
		int best = at("e4");
		
		// e4 will have 20 wins and e5 will have 12.
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

}
