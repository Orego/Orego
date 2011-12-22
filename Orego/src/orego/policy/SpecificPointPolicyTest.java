package orego.policy;

import static org.junit.Assert.*;
import static orego.core.Coordinates.*;
import orego.mcts.*;
import orego.core.Board;
import org.junit.Before;
import org.junit.Test;
import ec.util.MersenneTwisterFast;

public class SpecificPointPolicyTest {

	private Board board;

	private SpecificPointPolicy policy;

	private MersenneTwisterFast random;

	@Before
	public void setUp() throws Exception {
		board = new Board();
		random = new MersenneTwisterFast();
		policy = new SpecificPointPolicy();
	}

	@Test
	public void testSelectAndPlayOneMove() {
		policy = new SpecificPointPolicy();
		int result = policy.selectAndPlayOneMove(random, board);
		assertEquals(NO_POINT, result);
	}

	@Test
	public void testUpdatePriors() {
		SearchNode node = new SearchNode();
		node.reset(board.getHash());
		policy = new SpecificPointPolicy(at("e3"));
		policy.updatePriors(node, board, 2);
		assertEquals(4, node.getRuns(at("e3")));
		assertEquals(3, node.getWins(at("e3")));
	}

}
