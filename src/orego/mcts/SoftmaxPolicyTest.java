package orego.mcts;

import static orego.core.Colors.BLACK;
import static orego.patterns.Pattern.diagramToNeighborhood;
import static orego.patterns.Pattern.getLowestTransformation;
import static orego.core.Coordinates.at;
import static org.junit.Assert.*;

import orego.core.Board;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class SoftmaxPolicyTest {

	private Board board;
	
	@Before
	public void setup() {
		board = new Board();
	}
	
	@Test
	public void testShouldPreferHigherWeights() {
		String[] problem = new String[] {
			"...................",//19
			"...................",//18
			"...................",//17
			"...................",//16
			"...................",//15
			"...................",//14
			"...................",//13
			"...................",//12
			"...................",//11
			"...................",//10
			"...................",//9
			"...................",//8
			"...................",//7
			"...................",//6
			"...................",//5
			"...................",//4
			".#O................",//3
			"..#................",//2
			"..................." //1
		  // ABCDEFGHJKLMNOPQRST
		};
		board.setUpProblem(BLACK, problem);
		double[] weights = new double[Character.MAX_VALUE];
		char testPattern = getLowestTransformation(diagramToNeighborhood(".#O\n..#\n..."));
		weights[testPattern] = 10.0f;
		SoftmaxPolicy policy = new SoftmaxPolicy(weights);
		int selected = policy.selectAndPlayOneMove(new MersenneTwisterFast(), board);
		assertEquals(selected, at("b2"));
	}

}
