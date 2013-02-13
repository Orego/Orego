package orego.cluster;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static orego.core.Coordinates.*;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import orego.play.UnknownPropertyException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClusterPlayerTest {
	
	private static class MockTreeSearcher implements TreeSearcher {
		
		protected SearchController controller;
		protected boolean resetDone;
		protected double komi;
		protected Map<String, String> properties = new HashMap<String, String>();
		protected List<Integer> moves = new ArrayList<Integer>();
		protected boolean beginSearchDone;
		protected int bestMove = -1;
		protected String player_name;
		
		@Override
		public void setController(SearchController c) throws RemoteException {
			controller = c;
		}

		@Override
		public void reset() throws RemoteException {
			resetDone = true;
		}

		@Override
		public void setKomi(double k) throws RemoteException {
			komi = k;
		}

		@Override
		public void setProperty(String key, String value)
				throws RemoteException {
			properties.put(key, value);
		}

		@Override
		public void acceptMove(int player, int location) throws RemoteException {
			moves.add(location);
		}

		@Override
		public void beginSearch() throws RemoteException {
			beginSearchDone = true;
			final TreeSearcher thisSearcher = this;
			final int bestMoveLoc = bestMove;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					int[] runs = new int[FIRST_POINT_BEYOND_BOARD];
					Arrays.fill(runs, 2);
					int[] wins = new int[FIRST_POINT_BEYOND_BOARD];
					Arrays.fill(wins, 1);
					if(bestMoveLoc >= 0) {
						runs[bestMoveLoc] += 10;
						wins[bestMoveLoc] += 10;
					}
					try {
						controller.acceptResults(thisSearcher, runs, wins);
					} catch (RemoteException e) {
						System.err.println("Controller refused to accept fake results!");
					}
				}
			}).start();
		}
		
		@Override
		public String toString() {
			return "MockTreeSearcher";
		}

		@Override
		public boolean setPlayer(String player) throws RemoteException {
			this.player_name = player;
			return true;
		}
	}

	private static final String HOST_A = "hosta.local";
	
	@Mock private ClusterPlayer.RegistryFactory mockFactory;
	@Mock private Registry mockRegistryA;
	private ClusterPlayer player;
	private MockTreeSearcher searcher;
	
	@Before
	public void setUp() throws Exception {
		player = new ClusterPlayer();
		player.setProperty("search_hosts", HOST_A);
		searcher = new MockTreeSearcher();
		player.factory = mockFactory;
		when(mockFactory.locateRegistry(HOST_A)).thenReturn(mockRegistryA);
		when(mockRegistryA.lookup(TreeSearcher.SEARCHER_NAME)).thenReturn(searcher);
	}

	/* Tests relating to setup */
	@Test
	public void testShouldResetSearchers() {
		player.reset();
		assertTrue(searcher.resetDone);
	}
	
	@Test
	public void testShouldSetKomi() {
		double testKomi = 6.5;
		player.reset();
		player.setKomi(testKomi);
		assertEquals(testKomi, searcher.komi, 0.001);
	}
	
	/* Tests relating to properties */
	@Test
	public void testShouldForwardProperties() throws UnknownPropertyException {
		String keyA = "msec"; // Use 'msec' here beacuse cluster player treats it specially
		String valA = "1000";
		String keyB = "ponder";
		String valB = "true";
		player.reset();
		player.setProperty(keyA, valA);
		player.setProperty(keyB, valB);
		assertTrue(searcher.properties.containsKey(keyA));
		assertTrue(searcher.properties.get(keyA) == valA);
		assertTrue(searcher.properties.containsKey(keyB));
		assertTrue(searcher.properties.get(keyB) == valB);
	}
	
	@Test
	public void testShouldNotSetHosts() throws UnknownPropertyException {
		player.reset();
		player.setProperty("search_hosts", HOST_A);
		assertFalse(searcher.properties.containsKey(HOST_A));
	}
	
	@Test
	public void testShouldSendOldProperties() throws UnknownPropertyException {
		String key = "ponder";
		String val = "true";
		player.setProperty(key, val);
		player.reset();
		assertTrue(searcher.properties.containsKey(key));
		assertTrue(searcher.properties.get(key) == val);
	}
	
	@Test
	public void testShouldParseMoveTime() throws UnknownPropertyException {
		String key = "msec";
		long msec = 1200;
		String val = String.valueOf(msec);
		player.reset();
		player.setProperty(key, val);
		assertEquals(msec, player.getMillisecondsPerMove());
	}
	
	/* Tests relating to moves */
	@Test
	public void testShouldSendMoves() {
		player.reset();
		ArrayList<Integer> moves = new ArrayList<Integer>();
		moves.add(at("a1"));
		moves.add(at("a2"));
		player.acceptMove(moves.get(0));
		player.acceptMove(moves.get(1));
		assertEquals(searcher.moves, moves);
	}
	
	/* Tests related to move generation */
	@Test
	public void testSetsController() {
		player.reset();
		assertTrue(searcher.controller != null);
	}
	
	@Test
	public void testShouldRequestSearch() {
		player.reset();
		player.setOpeningBook(null);
		player.bestMove();
		assertTrue(searcher.beginSearchDone);
	}
	
	@Test
	public void testShouldUseSearchResults() {
		player.reset();
		player.setOpeningBook(null);
		int best = at("e4");
		searcher.bestMove = best;
		assertEquals(best, player.bestMove());
	}
	
	@Test
	public void testShouldUseOpeningBook() throws UnknownPropertyException {
		player.reset();
		// Use a simple book with deterministic behavior
		player.setProperty("book", "StarPointsBook");
		int expected = player.getOpeningBook().nextMove(player.getBoard());
		assertEquals(expected, player.bestMove());
	}
}
