package orego.heuristic;

import orego.core.*;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
import orego.util.*;

/**
 * The value of a move is the number of stones saved * the number of liberties
 * after saving - 1.
 */
public class EscapeHeuristic extends Heuristic {

	/** Endangered chains. */
	private IntList targets;
	
	/** Chains rescued by capturing. */
	private IntList saved;

	public EscapeHeuristic(int weight) {
		super(weight);
		targets = new IntList(4);
		saved = new IntList(BOARD_AREA);
	}

	@Override
	public int evaluate(int p, Board board) {
		int color = board.getColorToPlay();
		if(board.isSelfAtari(p, color)){
			return 0;
		}
		int result = 0;
		targets.clear();
		for (int i = 0; i < 4; i++) {
			int neighbor = NEIGHBORS[p][i];
			if (board.getColor(neighbor) == Colors.opposite(color)) {
				// Can capturing enemy chain save one or more of ours?
				int enemy = board.getChainId(neighbor);
				if ((board.isInAtari(enemy)) && (!targets.contains(enemy))) {
					targets.add(enemy);
					saved.clear();
					int next = neighbor;
					do {
						for (int j = 0; j < 4; j++) {
							int m = NEIGHBORS[next][j];
							int save = board.getChainId(m);
							if (board.getColor(m) == color && (board.isInAtari(save))) {
								if (!saved.contains(save)) {
									result += board.getChainSize(save) + board.getChainSize(enemy);
									saved.add(save);
								}
							}
						}
						next = board.getChainNextPoints()[next];
					} while (next != neighbor);
				}
			} else if (board.getColor(neighbor) == color) {
				// Can we save our chain by extending?
				int target = board.getChainId(neighbor);
				if ((board.isInAtari(target)) && (!targets.contains(target))) {
					targets.add(target);
					result += board.getChainSize(target);
				}
			}
		}
		return result;
	}

}
