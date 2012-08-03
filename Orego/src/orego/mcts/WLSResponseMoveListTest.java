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
		
		assertEquals(FIRST_POINT_BEYOND_BOARD, responses.getMovesWLS().length);
		
		responses = new WLSResponseMoveList(16);
		
		assertEquals(16, responses.getTopResponsesLength());
		
		assertEquals(16, responses.getTopResponses().length);
		
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
	public void rankMove() {
		// add moves with ascending wins
		
		String movePrefix = "a";
		for (int i = 0; i < responses.getTopResponsesLength(); i++) {
			String move = movePrefix + (i + 1);
			
			for (int j = 0; j < i + 1; j++) {
				responses.addWin(at(move));
			}
		}
		
				
		// make certain the top list contains all these moves
		// a8, a7, a6, ...., a1
		for (int i = 0; i < responses.getTopResponsesLength(); i++) {
			// look at the moves in *descending* order while looking in the slots in *ascending* order
			// Remember, the best moves are at the beginning of the list
			assertEquals(at(movePrefix + (responses.getTopResponsesLength() - i)), responses.getTopResponses()[i]);
		}
		
		
				
		// now add 9 wins (more than the highest element) 
		// to ensure the new move bubbles to the top of the list
		
		for (int i = 0; i < responses.getTopResponsesLength() + 1; i++) {
			responses.addWin(at("b9"));
		}
		
		
		// move should now be at the top of the list
		assertEquals(at("b9"), responses.getTopResponses()[0]);
		
		// now add a move to the middle
		for (int i = 0; i < responses.getTopResponsesLength() / 2; i++) {
			responses.addWin(at("b4"));
		}
		
		// it will be bumped down to the two spot (all shifted down since 9 was added, and below 4)
		assertEquals(at("b4"), responses.getTopResponses()[6]);
		
		// now test adding some losses and ensure the moves do not move
		// add some losses to b9 and watch it fall!
		for (int i = 0; i < responses.getTopResponsesLength() - 4; i++) {
			responses.addLoss(at("b9"));
		}
		
		// now promote little b4 up the list
		for (int i = 0; i < 5; i++) {
			responses.addWin(at("b4"));
		}
		// make sure b4 is the new head honcho
		assertEquals(at("b4"), responses.getTopResponses()[0]);
		
		assertEquals(at("b9"), responses.getTopResponses()[1]);
		
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
	}
}
