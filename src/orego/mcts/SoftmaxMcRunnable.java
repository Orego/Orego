package orego.mcts;

import static orego.core.Coordinates.NO_POINT;
import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.heuristic.HeuristicList;

public class SoftmaxMcRunnable extends LgrfMcRunnable {

	private SoftmaxPolicy defaultPolicy;
	
	public SoftmaxMcRunnable(McPlayer player, HeuristicList heuristics, int[][] replies1, int[][][] replies2, SoftmaxPolicy policy) {
		super(player, heuristics, replies1, replies2);
		
		defaultPolicy = policy;
	}
	
	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int lgrfMove = selectAndPlayLgrfMove(random, board);
		if(lgrfMove != NO_POINT) {
			return lgrfMove;
		}
		return defaultPolicy.selectAndPlayOneMove(random, board);
	}

}
