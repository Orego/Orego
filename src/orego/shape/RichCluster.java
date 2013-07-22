package orego.shape;

import static orego.core.Board.MAX_PATTERN_RADIUS;
import static orego.core.Colors.*;
import orego.core.Board;

/** Like a Cluster, but also incorporates global search (reply to a whole-board position) and local search (reply to recent moves). */
public class RichCluster extends Cluster {

	private static final long serialVersionUID = 1L;

	/** Table for responses to the global board position, indexed by color to move. */
	private Table[] global;
	
	/** Table for responses to the last move, indexed by color to move. */
	private Table[] reply1;
	
	public RichCluster(int tables, int bits) {
		super(tables, bits);
		global = new Table[] {new Table(tables, bits), new Table(tables, bits)};
		reply1 = new Table[] {new Table(tables, bits), new Table(tables, bits)};
	}
	
	@Override
	public void store(Board board, int move, int win) {
		super.store(board, move, win);
		int color = board.getColorToPlay();
		long globalHash = board.getHash() ^ Board.ZOBRIST_HASHES[color][move];
		global[color].store(globalHash, win);
		long reply1Hash = Board.ZOBRIST_HASHES[opposite(color)][board.getMove(board.getTurn() - 1)] ^ Board.ZOBRIST_HASHES[color][move];
		reply1[color].store(reply1Hash, win);
	}

	@Override
	public float getWinRate(Board board, int move) {
		int color = board.getColorToPlay();
		long globalHash = board.getHash() ^ Board.ZOBRIST_HASHES[color][move];
		long reply1Hash = Board.ZOBRIST_HASHES[opposite(color)][board.getMove(board.getTurn() - 1)] ^ Board.ZOBRIST_HASHES[color][move];
		return (super.getWinRate(board, move) + global[color].getWinRate(globalHash) + reply1[color].getWinRate(reply1Hash)) / 3.0f;
	}

}
