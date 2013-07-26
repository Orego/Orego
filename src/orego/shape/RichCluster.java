package orego.shape;

import static orego.core.Colors.*;
import static orego.core.Board.MAX_PATTERN_RADIUS;

import orego.core.Board;

/** Like a Cluster, but also incorporates global search (reply to a whole-board position). */
public class RichCluster extends Cluster {

	private static final long serialVersionUID = 1L;

	/** Table for responses to the global board position, indexed by color to move. */
	private Table[] global;
		
	public RichCluster(int tables, int bits) {
		super(tables, bits);
		global = new Table[] {new Table(tables, bits), new Table(tables, bits)};
	}

	@Override
	public void setCount(long count) {
		super.setCount(count);
		for (int color = BLACK; color <= WHITE; color++) {
			global[color].setCount(count);
		}
	}

	@Override
	public long getCount(Board board, int move) {
		int color = board.getColorToPlay();
		long globalHash = getGlobalHash(board, move, color);
		return (super.getCount(board, move) + global[color].getCount(globalHash)) / 2;
	}

	@Override
	public void store(Board board, int move, int win) {
		super.store(board, move, win);
		int color = board.getColorToPlay();
		long globalHash = getGlobalHash(board, move, color);
		global[color].store(globalHash, win);
	}

	protected long getGlobalHash(Board board, int move, int color) {
		return board.getHash() ^ Board.ZOBRIST_HASHES[color][move];
	}

	@Override
	public void store(long[] hashes, int color, int win) {
		super.store(hashes, color, win);
		global[color].store(hashes[MAX_PATTERN_RADIUS + 1], win);
	}

	@Override
	public float getWinRate(Board board, int move) {
		int color = board.getColorToPlay();
		long globalHash = getGlobalHash(board, move, color);
		return (4 * super.getWinRate(board, move) + 1 * global[color].getWinRate(globalHash)) / 5.0f;
	}

}
