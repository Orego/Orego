package orego.policy;

import static orego.core.Board.PLAY_OK;
import static orego.core.Colors.*;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.core.Coordinates.ON_BOARD;
import static orego.patterns.Pattern.*;
import orego.core.Board;
import orego.mcts.SearchNode;
import orego.patterns.*;
import orego.util.*;
import ec.util.MersenneTwisterFast;

public class FillBoardPolicy extends Policy {
	
	public static final char EMPTY_NEIGHBORHOOD = diagramToNeighborhood("...\n. .\n...");

	public FillBoardPolicy(Policy fallback) {
		super(fallback);
		// TODO Auto-generated constructor stub
	}

	public FillBoardPolicy() {
		this(new RandomPolicy());
	}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean hasAllVacantNeighbors(int p, Board board) {
		return (board.getNeighborhood(p)==EMPTY_NEIGHBORHOOD);
	}

}
