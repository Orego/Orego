package orego.cluster;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;

import orego.cluster.RMIStartup.RegistryFactory;
import orego.play.UnknownPropertyException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ClusterPlayerTest {
	
	@Mock private RegistryFactory mockFactory;
	@Mock private Registry mockRegistry;
	@Mock TreeSearcher searcher;
	
	private ClusterPlayer player;
	
	@Before
	public void setUp() throws Exception {
		when(mockFactory.getRegistry()).thenReturn(mockRegistry);
		ClusterPlayer.factory = mockFactory;
		player = new ClusterPlayer();
		//searcher = new MockTreeSearcher(player);
		player.reset();
		player.addSearcher(searcher);
	}
	
	/** 
	 * Stubs out the beginSearch method on the given mock tree searcher.
	 * The new beginSearch method will respond after msecToRespond with the best
	 * move at bestMove.
	 */
	protected void setupMockSearcher(TreeSearcher mockSearcher, final int msecToRespond, final int bestMove, final int runsPerPoint, final int winsPerPoint) throws RemoteException {
		doAnswer(new Answer<Object>() {
			
			@Override
			public Object answer(InvocationOnMock invocation) {
				final TreeSearcher mock = (TreeSearcher) invocation.getMock();
				
				// Begin a new thread that will wait the specified time and then call the server back
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							Thread.sleep(msecToRespond);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						long[] runs = new long[FIRST_POINT_BEYOND_BOARD];
						Arrays.fill(runs, runsPerPoint);
						long[] wins = new long[FIRST_POINT_BEYOND_BOARD];
						Arrays.fill(wins, winsPerPoint);
						if(bestMove >= 0) {
							runs[bestMove] += 10;
							wins[bestMove] += 10;
						}
						try {
							player.acceptResults(mock, runs, wins);
						} catch (RemoteException e) {
							System.err.println("Controller refused to accept fake results!");
						}
						
					}
					
				}).start();
				
				return null;
			}
			
		}).when(mockSearcher).beginSearch();
	}
	
	protected void setupMockSearcher(TreeSearcher mockSearcher, final int msecToRespond, final int bestMove) throws RemoteException {
		setupMockSearcher(mockSearcher, msecToRespond, bestMove, 1, 2);
	}

	/* Tests relating to setup */
	@Test
	public void testShouldPublish() throws AccessException, RemoteException {
		verify(mockRegistry).rebind(eq(SearchController.SEARCH_CONTROLLER_NAME), (Remote) any());
	}
	
	@Test
	public void testShouldResetSearchers() throws RemoteException {
		player.reset();
		verify(searcher, atMost(2)).reset();
	}
	
	@Test
	public void testShouldSetKomi() throws RemoteException {
		double testKomi = 6.5;
		player.setKomi(testKomi);
		verify(searcher).setKomi(testKomi);
	}
	
	/* Tests relating to properties */
	@Test
	public void testShouldForwardProperties() throws UnknownPropertyException, RemoteException {
		String keyA = "msec"; // Use 'msec' here beacuse cluster player treats it specially
		String valA = "1000";
		String keyB = "ponder";
		String valB = "true";
		player.setProperty(keyA, valA);
		player.setProperty(keyB, valB);
		verify(searcher).setProperty(keyA, valA);
		verify(searcher).setProperty(keyB, valB);
	}
		
	@Test
	public void testShouldSendOldProperties() throws UnknownPropertyException, RemoteException {
		String key = "ponder";
		String val = "true";
		player.setProperty(key, val);
		TreeSearcher s = mock(TreeSearcher.class);
		player.addSearcher(s);
		verify(s).setProperty(key, val);
	}
	
	@Test
	public void testShouldSetPlayer() throws UnknownPropertyException, RemoteException {
		String val = "MCTSPlayer";
		// Use a new cluster player so we can set the remote_player prop
		// before adding the searcher
		ClusterPlayer p = new ClusterPlayer();
		p.reset();
		p.setProperty("remote_player", val);
		p.addSearcher(searcher);
		verify(searcher).setPlayer(val);
	}
	
	@Test
	public void testShouldResetAfterSetPlayer() throws RemoteException, UnknownPropertyException {
		String val = "MCTSPlayer";
		TreeSearcher s = mock(TreeSearcher.class);
		ClusterPlayer p = new ClusterPlayer();
		p.setProperty("remote_player", val);
		p.reset();
		p.addSearcher(s);
		InOrder inOrder = inOrder(s);
		inOrder.verify(s).setPlayer(val);
		inOrder.verify(s).reset();
	}
	
	@Test
	public void testShouldNotResetPlayer() throws UnknownPropertyException, RemoteException {
		// Once a remote searcher has connected and had a player set, do not change it
		String playerClass = "SomePlayer";
		player.setProperty("remote_player", playerClass);
		verify(searcher, never()).setPlayer(playerClass);
	}
	
	@Test
	public void testShouldNotSendPlayerAsProperty() throws UnknownPropertyException, RemoteException {
		String playerClass = "SomePlayer";
		player.setProperty("remote_player", playerClass);
		TreeSearcher s = mock(TreeSearcher.class);
		player.addSearcher(s);
		verify(s, never()).setProperty("remote_player", playerClass);
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
	public void testShouldSendMoves() throws RemoteException {
		int[] moves = new int[] {at("a1"), at("a2")};
		player.acceptMove(moves[0]);
		player.acceptMove(moves[1]);
		verify(searcher).acceptMove(BLACK, moves[0]);
		verify(searcher).acceptMove(WHITE, moves[1]);
	}
	
	@Test
	public void testShouldSendOldMoves() throws RemoteException {
		int[] moves = new int[] {at("a1"), at("a2")};
		player.acceptMove(moves[0]);
		player.acceptMove(moves[1]);
		TreeSearcher s = mock(TreeSearcher.class);
		player.addSearcher(s);
		InOrder inOrder = inOrder(s);
		inOrder.verify(s).acceptMove(BLACK, moves[0]);
		inOrder.verify(s).acceptMove(WHITE, moves[1]);
	}
	
	@Test
	public void testShouldForwardUndo() throws RemoteException {
		when(searcher.undo()).thenReturn(true);
		int move = at("a1");
		player.acceptMove(move);
		assertTrue(player.undo());
		verify(searcher).undo();
	}
	
	/* Tests related to move generation */
	
	@Test
	public void testShouldRequestSearch() throws RemoteException {
		setupMockSearcher(searcher, 100, -1);
		
		player.setOpeningBook(null);
		player.bestMove();
		verify(searcher).beginSearch();
	}
	
	@Test
	public void testShouldUseSearchResults() throws RemoteException {
		player.setOpeningBook(null);
		int best = at("e4");
		setupMockSearcher(searcher, 100, best);
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
	public void testShouldClearResults() throws RemoteException {
		player.setOpeningBook(null);
		int bestA = at("d3");
		setupMockSearcher(searcher, 100, bestA);
		player.bestMove();
		int bestB = at("e4");
		setupMockSearcher(searcher, 100, bestB);
		assertEquals(bestB, player.bestMove());
	}
	
	@Test
	public void testShouldNotUseDepartedSearcher() throws RemoteException {
		player.setOpeningBook(null);
		player.removeSearcher(searcher);
		reset(searcher);
		player.bestMove();
		verifyZeroInteractions(searcher);
	}
	
	@Test
	public void testShouldNotPlayIllegalMove() throws RemoteException {
		int bestButIllegal = at("a1");
		player.setOpeningBook(null);
		player.acceptMove(bestButIllegal);
		setupMockSearcher(searcher, 100, bestButIllegal);
		assertFalse(player.bestMove() == bestButIllegal);
	}
	
	@Test
	public void testShouldResign() throws RemoteException {
		player.setOpeningBook(null);
		setupMockSearcher(searcher, 100, -1, 11, 1);
		assertEquals(RESIGN, player.bestMove());
	}
}
