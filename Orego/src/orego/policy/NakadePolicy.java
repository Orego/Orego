package orego.policy;

import orego.core.Board;
import orego.core.Colors;
import orego.core.Coordinates;
import ec.util.MersenneTwisterFast;
import static orego.core.Colors.*;

public class NakadePolicy extends Policy {

	public NakadePolicy(Policy fallback) {
		super(fallback);
	}

	public boolean isNakadePoint(int lastMove, Board board) {
		int n;
		int count = 0;
		int enemy = 0;
		int other = 0;
		int eye = 0;
		for (int i = 0; i < 4; i++) {
			n = Coordinates.NEIGHBORS[lastMove][i];
			if (board.getColor(n) == VACANT) {
				for (int j = 0; j < 4; j++) {
					int m = Coordinates.NEIGHBORS[n][j];
					if (board.getColor(m) == VACANT) {
						count++;
					} else if (board.getColor(m) == Colors.opposite(board.getColorToPlay())) {
						enemy++;
					}
				}
				if (count == 2 && enemy == 2) {
					eye++;
				} else if (count == 1 && enemy == 3) {
					other++;
				}
			}
		}
		return (eye == 1 && other == 2);
	}
	
	public int findNakade(int lastMove, Board board) {
		int neighbor;
		int[] eyeLiberties = new int[2];
		int vacant = 0;
		int eye = 0;
		for(int x = 0; x < 4; x++) {
			neighbor = Coordinates.NEIGHBORS[lastMove][x];
			if(board.getColor(neighbor) == VACANT) {
				vacant = conditionOne(neighbor,board);
				eyeLiberties = conditionTwo(neighbor,board);
				if(vacant > -1) {
					eyeLiberties = conditionTwo(vacant,board);
					if(eyeLiberties != null) {
						eye = vacant;
						vacant = (eyeLiberties[0]==vacant) ? eyeLiberties[1] : eyeLiberties[0];
						if(conditionOne(vacant,board) > -1) {
							return eye;
						}
					}
				} else if(eyeLiberties!=null) {
					int lib0 = conditionOne(eyeLiberties[0],board);
					int lib1 = conditionOne(eyeLiberties[1],board);
					if(lib0 > -1 && lib1 > -1) {
						return neighbor;
					}
				}
			}
		}
		return -1;
	}
	
	public int conditionOne(int point, Board board) {
		int libertyCount = 0;
		int enemyCount = 0;
		int liberty = 0;
		assert board.getColor(point) != OFF_BOARD_COLOR : "\nTrying to get neighbors of no point";
		for(int i = 0; i < 4; i++) {
			int p = Coordinates.NEIGHBORS[point][i];
			if(board.getColor(p) == VACANT) {
				libertyCount++;
				liberty = p;
			} else if(board.getColor(p) != board.getColorToPlay()) {
				enemyCount++;
			}
		}
		if(libertyCount == 1 && enemyCount == 3) {
			return liberty;
		}
		return -1;		
	}
	
	public int[] conditionTwo(int point, Board board) {
		int libertyCount = 0;
		int enemyCount = 0;
		int liberties[] = new int[2];
		for(int i = 0; i < 4; i++) {
			int p = Coordinates.NEIGHBORS[point][i];
			if(board.getColor(p) == VACANT) {
				if(libertyCount <= 1) {
					liberties[libertyCount] = p;
					libertyCount++;
				}
			}else if(board.getColor(p) != board.getColorToPlay()) {
				enemyCount++;
			}
		}
		if(libertyCount == 2 && enemyCount == 2) {
			return liberties;
		}
		return null;
}

	@Override
	public int selectAndPlayOneMove(MersenneTwisterFast random, Board board) {
		int lastPlay = board.getMove(board.getTurn() - 1);
		int move = findNakade(lastPlay,board);
		if(move > -1) {
			return move;
		}
		return getFallback().selectAndPlayOneMove(random, board);
	}

}
