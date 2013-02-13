package orego.cluster;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static orego.core.Coordinates.*;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import orego.cluster.ClusterPlayer.RegistryFactory;
import orego.play.UnknownPropertyException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ClusterPlayerTest {
	
	private static class MockTreeSearcher implements TreeSearcher {
		
		protected boolean resetDone;
		protected double komi;
		protected Map<String, String> properties = new HashMap<String, String>();
		protected List<Integer> moves = new ArrayList<Integer>();
		protected boolean beginSearchDone;
		protected int bestMove = -1;
		protected String playerName;
		protected ClusterPlayer controller;

		public MockTreeSearcher(ClusterPlayer c){
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
						Thread.sleep(100);
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
			this.playerName = player;
			return true;
		}
	}
	
	@Mock private RegistryFactory mockFactory;
	@Mock private Registry mockRegistry;
	private ClusterPlayer player;
	private MockTreeSearcher searcher;
	
	@Before
	public void setUp() throws Exception {
		when(mockFactory.getRegistry()).thenReturn(mockRegistry);
		ClusterPlayer.factory = mockFactory;
		player = new ClusterPlayer();
		searcher = new MockTreeSearcher(player);
		player.reset();
		player.addSearcher(searcher);
	}

	/* Tests relating to setup */
	@Test
	public void testShouldPublish() throws AccessException, RemoteException {
		verify(mockRegistry).rebind(SearchController.SEARCH_CONTROLLER_NAME, player);
	}
	
	@Test
	public void testShouldResetSearchers() {
		searcher.resetDone = false;
		player.reset();
		assertTrue(searcher.resetDone);
	}
	
	@Test
	public void testShouldSetKomi() {
		double testKomi = 6.5;
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
		player.setProperty(keyA, valA);
		player.setProperty(keyB, valB);
		assertTrue(searcher.properties.containsKey(keyA));
		assertTrue(searcher.properties.get(keyA) == valA);
		assertTrue(searcher.properties.containsKey(keyB));
		assertTrue(searcher.properties.get(keyB) == valB);
	}
		
	@Test
	public void testShouldSendOldProperties() throws UnknownPropertyException {
		String key = "ponder";
		String val = "true";
		player.setProperty(key, val);
		MockTreeSearcher s = new MockTreeSearcher(player);
		player.addSearcher(s);
		assertTrue(s.properties.containsKey(key));
		assertTrue(s.properties.get(key) == val);
	}
	
	@Test
	public void testShouldSetPlayer() throws UnknownPropertyException {
		String val = "MCTSPlayer";
		// Use a new cluster player so we can set the remote_player prop
		// before adding the searcher
		ClusterPlayer p = new ClusterPlayer();
		p.reset();
		p.setProperty("remote_player", val);
		p.addSearcher(searcher);
		assertEquals(val, searcher.playerName);
	}
	
	@Test
	public void testShouldNotResetPlayer() throws UnknownPropertyException {
		// Once a remote searcher has connected and had a player set, do not change it
		player.setProperty("remote_player", "SomePlayer");
		assertEquals("Lgrf2Player", searcher.playerName);
	}
	
	@Test
	public void testShouldParseMoveTime() throws UnknownPropertyException {
		String key = "msec";
		long msec = 1200;
		String val = String.valueOf(msec);
		player.setProperty(key, val);
		assertEquals(msec, player.getMillisecondsPerMove());
	}
	
	/* Tests relating to moves */
	@Test
	public void testShouldSendMoves() {
		ArrayList<Integer> moves = new ArrayList<Integer>();
		moves.add(at("a1"));
		moves.add(at("a2"));
		player.acceptMove(moves.get(0));
		player.acceptMove(moves.get(1));
		assertEquals(searcher.moves, moves);
	}
	
	/* Tests related to move generation */
	
	@Test
	public void testShouldRequestSearch() {
		player.setOpeningBook(null);
		player.bestMove();
		assertTrue(searcher.beginSearchDone);
	}
	
	@Test
	public void testShouldUseSearchResults() {
		player.setOpeningBook(null);
		int best = at("e4");
		searcher.bestMove = best;
		assertEquals(best, player.bestMove());
	}
	
	@Test
	public void testShouldUseOpeningBook() throws UnknownPropertyException {
		// Use a simple book with deterministic behavior
		player.setProperty("book", "StarPointsBook");
		int expected = player.getOpeningBook().nextMove(player.getBoard());
		assertEquals(expected, player.bestMove());
	}
	
	@Test
	public void testShouldClearResults() {
		player.setOpeningBook(null);
		int bestA = at("e6");
		searcher.bestMove = bestA;
		player.bestMove();
		int bestB = at("e4");
		searcher.bestMove = bestB;
		assertEquals(bestB, player.bestMove());
	}
}
