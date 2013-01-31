package orego.mcts;

import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;
import orego.core.Coordinates;
import orego.wls.State;
import orego.wls.WinLossStates;

import org.junit.Before;
import org.junit.Test;

public class WLSResponseMoveListTest {
	
	private WLSResponseMoveList responses;
	@Before
	public void setup() {
		responses = new WLSResponseMoveList(8);
	}
	
	@Test
	public void testConstructors() {
		assertEquals(8, responses.getTopResponsesLength());
		
		assertEquals(8, responses.getTopResponses().length);
		
		assertEquals(8, responses.getTopResponsesIllegality().length);
		
		assertEquals(FIRST_POINT_BEYOND_BOARD, responses.getMovesWLS().length);
		
		responses = new WLSResponseMoveList(16);
		
		assertEquals(16, responses.getTopResponsesLength());
		
		assertEquals(16, responses.getTopResponses().length);
		
		assertEquals(16, responses.getTopResponsesIllegality().length);
		
		assertEquals(FIRST_POINT_BEYOND_BOARD, responses.getMovesWLS().length);
		
		// make certain an uninitialized move is at WLS state 0/0
		State initState = responses.getWLSState(at("m8"));
		
		assertNotNull(initState);
		
		assertEquals(0, initState.getWins());
		assertEquals(0, initState.getLosses());
		
		// should be in zero state for move NO_POINT (and for all moves for that matter)
		assertEquals(0, responses.getMovesWLS()[Coordinates.NO_POINT]);
		
		// pick a few points from the top responses to ensure they are NO_POINT by default
		assertEquals(Coordinates.NO_POINT, responses.getTopResponses()[0]);
		
		assertEquals(Coordinates.NO_POINT, responses.getTopResponses()[3]);
		
		assertEquals(Coordinates.NO_POINT, responses.getTopResponses()[5]);
		
		assertEquals(Coordinates.NO_POINT, responses.getTopResponses()[7]);
	}
	
	@Test
	public void testIllegalityCounters() {
		responses.getTopResponses()[0] = at("m2");
		responses.addIllegalPlay(0);
		
		assertEquals(1, responses.getIllegality(0));
		
		responses.clearIllegality(0);
		
		assertEquals(0, responses.getIllegality(0));
		
		responses.addIllegalPlay(0);
		responses.addIllegalPlay(0);
		
		assertEquals(2, responses.getIllegality(0));
		
		responses.clearIllegality(0);
		
		assertEquals(0, responses.getIllegality(0));
		
		assertEquals(at("m2"), responses.getTopResponses()[0]);
	}
	
	@Test
	public void testGetWLSMoveState() {
		// set a particular move to state 1/1 (index 180)
		responses.getMovesWLS()[at("c5")] = (short) WinLossStates.getWLS().findStateIndex(1, 1);
		
		State moveState = responses.getWLSState(at("c5"));
		
		assertNotNull(moveState);
		
		assertEquals(1, moveState.getWins());
		assertEquals(1, moveState.getRuns());
		
		// check to make sure all uninitialized moves default to 0/0
		
		State initState = responses.getWLSState(at("d8"));
		
		assertNotNull(initState);
		
		assertEquals(0, initState.getWins());
		assertEquals(0, initState.getLosses());
	}
	
	@Test
	public void testInTopResponses() {
		// TODO: try a few more indicies
		// make sure a random move isn't in the list
		
		assertFalse(responses.inTopResponses(at("l2")));
		
		// add a move to the top responses list
		responses.getTopResponses()[2] = at("d6");
		
		assertTrue(responses.inTopResponses(at("d6")));
		
		// add another move
		responses.getTopResponses()[7] = at("e3");
		
		assertTrue(responses.inTopResponses(at("e3")));
	}
	
	@Test
	public void testAddWinRanking() {
		// fill the top response list with moves
		responses.addWin(at("b5"));
		responses.addWin(at("g6"));
		responses.addWin(at("f7"));
		responses.addWin(at("l4"));
		
		responses.addWin(at("e3"));
		responses.addWin(at("d2"));
		responses.addWin(at("c1"));
		responses.addWin(at("a8"));
		
		// make sure the moves are entered appropriately
		assertEquals(at("b5"), responses.getTopResponses()[0]);
		assertEquals(at("g6"), responses.getTopResponses()[1]);
		assertEquals(at("f7"), responses.getTopResponses()[2]);
		assertEquals(at("l4"), responses.getTopResponses()[3]);
		
		assertEquals(at("e3"), responses.getTopResponses()[4]);
		assertEquals(at("d2"), responses.getTopResponses()[5]);
		assertEquals(at("c1"), responses.getTopResponses()[6]);
		assertEquals(at("a8"), responses.getTopResponses()[7]);
		
		// now make some moves illegal
		for (int i = 0; i < WLSPlayer.MAX_ILLEGALITY_CAP; i++) {
			responses.addIllegalPlay(0);
			responses.addIllegalPlay(7);
			responses.addIllegalPlay(4);
		}
		
		// now add a win to another move (not in the top list)
		// and make sure it displaces an illegal move
		responses.addWin(at("k4"));
		
		assertEquals(at("k4"), responses.getTopResponses()[0]);
		
		responses.addWin(at("g3"));
		
		assertEquals(at("g3"), responses.getTopResponses()[4]);
		
		responses.addWin(at("m7"));
		
		assertEquals(at("m7"), responses.getTopResponses()[7]);
		
		
		// now add some additional wins to another move to force it into the list
		responses.addWin(at("o9"));
		responses.addWin(at("o9"));
		
		assertEquals(at("o9"), responses.getTopResponses()[0]);
		
		// now add some losses and let that move be replaced
		responses.addLoss(at("c1"));
		
		// try adding an existing move and ensure it doesn't work
		responses.addWin(at("o9"));
		
		assertEquals(at("c1"), responses.getTopResponses()[6]);
		
		// now add a new move and watch it replace the losing move c1
		responses.addWin(at("k9"));
		
		assertEquals(at("k9"), responses.getTopResponses()[6]);
	}
	
	@Test
	public void testSweep() {
		// fill the top responses list with a move that has some wins
		responses.addWin(at("b3"));
		responses.addWin(at("b3"));
		responses.addWin(at("b3"));
		for (int i = 0; i < responses.getTopResponsesLength(); i++) {
			responses.getTopResponses()[i] = at("b3");
		}
		
		// we now increment a few moves illegality counter and then sweep them
		responses.getTopResponses()[1] = at("b7");
		responses.getTopResponses()[7] = at("c3");
		responses.getTopResponses()[5] = at("a1");
		
		// push these moves to the illegality max
		for (int i = 0; i < WLSPlayer.MAX_ILLEGALITY_CAP; i++) {
			responses.addIllegalPlay(1);
			responses.addIllegalPlay(7);
			responses.addIllegalPlay(5);
		}
		
		responses.sweep();
		
		// make sure illegal moves were pruned
		assertEquals(Coordinates.NO_POINT, responses.getTopResponses()[1]);
		assertEquals(Coordinates.NO_POINT, responses.getTopResponses()[7]);
		assertEquals(Coordinates.NO_POINT, responses.getTopResponses()[5]);
		
		// make sure illegality counters were reset
		assertEquals(0, responses.getIllegality(1));
		assertEquals(0, responses.getIllegality(7));
		assertEquals(0, responses.getIllegality(5));
	
	}
	
	@Test
	public void testFindLowestWLS() {
		// fill the top responses array with moves that have at least 5
		responses.addWin(at("b3"));
		responses.addWin(at("b3"));
		responses.addWin(at("b3"));
		responses.addWin(at("b3"));
		responses.addWin(at("b3"));
		
		for (int i = 0; i < responses.getTopResponsesLength(); i++) {
			responses.getTopResponses()[i] = at("b3");
		}
		
		// setup some wins for a few moves and compare
		// +4 wins
		responses.addWin(at("b1"));
		responses.addWin(at("b1"));
		responses.addWin(at("b1"));
		responses.addWin(at("b1"));
		
		// +3 wins
		responses.addWin(at("b9"));
		responses.addWin(at("b9"));
		responses.addWin(at("b9"));
		
		responses.getTopResponses()[0] = at("b1");
		
		responses.getTopResponses()[7] = at("b9");
		
		// no ordering in top k list so only determined by WLS
		assertEquals(7, responses.findLowestWLS(at("b10")));
		
		// make sure we don't re-add an existing move (we pass in a proposed move)
		assertEquals(-1, responses.findLowestWLS(at("b1")));
		assertEquals(-1, responses.findLowestWLS(at("b3")));
	}
	
	@Test
	public void testAddWin() {
		int stateIndex = responses.getMovesWLS()[at("k5")];
		State initState = WinLossStates.getWLS().getState(stateIndex);
		
		assertEquals(0, initState.getWins());
		assertEquals(0, initState.getRuns());
		
		// add a win and transition to 1 / 1
		
		responses.addWin(at("k5"));
		
		stateIndex = responses.getMovesWLS()[at("k5")]; // new loss state
		State lossState = WinLossStates.getWLS().getState(stateIndex);
		
		assertEquals(1, lossState.getWins());
		assertEquals(1, lossState.getRuns());
		
		// since all other moves are 0 / 0 this move should be ranked at the top of the list "top responses"
		assertTrue(responses.inTopResponses(at("k5")));
		
		assertEquals(at("k5"), responses.getTopResponses()[0]);
	}
	
	@Test
	public void testAddLoss() {
		int stateIndex = responses.getMovesWLS()[at("k5")];
		State initState = WinLossStates.getWLS().getState(stateIndex);
		
		assertEquals(0, initState.getWins());
		assertEquals(0, initState.getRuns());
		
		// add a loss and transition to 0 / 1
		
		responses.addLoss(at("k5"));
		
		stateIndex = responses.getMovesWLS()[at("k5")]; // new loss state
		State lossState = WinLossStates.getWLS().getState(stateIndex);
		
		assertEquals(0, lossState.getWins());
		assertEquals(1, lossState.getRuns());
		
		// TODO: add a win, then a loss and ensure it deincrements
	}
	
	@Test
	public void testResizeTopResponsesList() {
		
		assertEquals(8, responses.getTopResponses().length);
		assertEquals(8, responses.getTopResponsesLength());
		
		responses.getTopResponses()[0] = at("j3");
		
		responses.resizeTopResponses(12);
		
		assertEquals(12, responses.getTopResponses().length);
		assertEquals(12, responses.getTopResponsesLength());
		
		// ensure we reset all top moves
		assertEquals(Coordinates.NO_POINT, responses.getTopResponses()[0]);
		
		
	}
}
