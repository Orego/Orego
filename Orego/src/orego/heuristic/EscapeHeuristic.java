package orego.heuristic;

import java.util.HashMap;

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
	
	/** 
	 * When we consider escaping by capturing enemy stones, we are interested
	 * in how many liberties the capture will gain for a friendly group in atari.
	 * libsFromCapture maps chain IDs to these counts of liberties gained.
	 */
	HashMap<Integer, Integer> libsFromCapture;

	public EscapeHeuristic(int weight) {
		super(weight);
		targets = new IntList(4);
		libsFromCapture = new HashMap<Integer, Integer>();
	}

	@Override
	public int evaluate(int p, Board board) {
		int color = board.getColorToPlay();
		if(board.isSelfAtari(p, color)){
			return 0;
		}
		libsFromCapture.clear();
		int result = 0;
		int libertiesGained = 0;
		targets.clear();
		for (int i = 0; i < 4; i++) {
			int neighbor = NEIGHBORS[p][i];
			if (board.getColor(neighbor) == Colors.opposite(color)) {
				// Can capturing enemy chain save one or more of ours?
				int enemy = board.getChainId(neighbor);
				if ((board.isInAtari(enemy)) && (!targets.contains(enemy))) {
					targets.add(enemy);
					int next = neighbor;
					do {
						for (int j = 0; j < 4; j++) {
							int m = NEIGHBORS[next][j];
							int save = board.getChainId(m);
							if (board.getColor(m) == color && (board.isInAtari(save))) {
								if (!libsFromCapture.containsKey(save)) {
									result += board.getChainSize(enemy);
									libsFromCapture.put(save, 1);
								}
								else {
									libsFromCapture.put(save, libsFromCapture.get(save)+1);
								}
							}
						}
						next = board.getChainNextPoints()[next];
					} while (next != neighbor);
					for (int chainId : libsFromCapture.keySet()) {
						result += board.getChainSize(chainId) * libsFromCapture.get(chainId);
					}
				}
			} else if (board.getColor(neighbor) == color) {
				// Can we save our chain by extending?
				int target = board.getChainId(neighbor);
				if ((board.isInAtari(target)) && (!targets.contains(target))) {
					targets.add(target);
					result += board.getChainSize(target);
					libertiesGained += board.getVacantNeighborCount(p);
					libertiesGained += Math.max(0, board.getLibertyCount(target) - 2);
				}
			}
		}
		return result * libertiesGained;
	}

}
