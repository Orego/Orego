package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

public class CaptureHeuristic implements Heuristic {
	
	private IntList neighborId;
	
	public CaptureHeuristic() {
		neighborId = new IntList(4);
	}
	
	@Override
	public int evaluate(int p, Board board) {
		int enemy = opposite(board.getColorToPlay());
		int result = 0;
		neighborId.clear();
		for (int i = 0; i < 4; i++) {
			int neighbor = NEIGHBORS[p][i];
			int chainId = board.getChainId(neighbor);
			if ((board.getColor(neighbor) == enemy) && (board.isInAtari(chainId)) && (!neighborId.contains(chainId))) {
				neighborId.add(chainId);
				result += board.getChainSize(neighbor);
			}
		}
		return result;
	}

}
