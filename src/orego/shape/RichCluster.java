package orego.shape;

import static orego.core.Colors.*;
import static orego.core.Board.MAX_PATTERN_RADIUS;

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
	public void setCount(long count) {
		super.setCount(count);
		for (int color = BLACK; color <= WHITE; color++) {
			global[color].setCount(count);
			reply1[color].setCount(count);
		}
	}

	@Override
	public long getCount(Board board, int move) {
		int color = board.getColorToPlay();
		long globalHash = getGlobalHash(board, move, color);
		long reply1Hash = getReply1Hash(board, move, color);
//		if (move == orego.core.Coordinates.at("n1")) {
//			System.err.println("Patterns: " + super.getCount(board, move));
//			System.err.println("Global: " + global[color].getCount(globalHash));
//			System.err.println("Reply1: " + reply1[color].getCount(reply1Hash));
//			System.err.println("Average" + ((super.getCount(board, move) + global[color].getCount(globalHash) + reply1[color].getCount(reply1Hash)) / 3));
//		}
		return (super.getCount(board, move) + global[color].getCount(globalHash) + reply1[color].getCount(reply1Hash)) / 3;
//		return (super.getCount(board, move) + global[color].getCount(globalHash)) / 2;
	}

	@Override
	public void store(Board board, int move, int win) {
		super.store(board, move, win);
		int color = board.getColorToPlay();
		long globalHash = getGlobalHash(board, move, color);
		global[color].store(globalHash, win);
		long reply1Hash = getReply1Hash(board, move, color);
		reply1[color].store(reply1Hash, win);
	}

	protected long getReply1Hash(Board board, int move, int color) {
		return Board.ZOBRIST_HASHES[opposite(color)][board.getMove(board.getTurn() - 1)] ^ Board.ZOBRIST_HASHES[color][move];
	}

	protected long getGlobalHash(Board board, int move, int color) {
		return board.getHash() ^ Board.ZOBRIST_HASHES[color][move];
	}

	@Override
	public void store(long[] hashes, int color, int win) {
		super.store(hashes, color, win);
		global[color].store(hashes[MAX_PATTERN_RADIUS + 1], win);
		reply1[color].store(hashes[MAX_PATTERN_RADIUS + 2], win);
	}

	@Override
	public float getWinRate(Board board, int move) {
		int color = board.getColorToPlay();
		long globalHash = getGlobalHash(board, move, color);
		long reply1Hash = getReply1Hash(board, move, color);
//		return (super.getWinRate(board, move) + global[color].getWinRate(globalHash) + reply1[color].getWinRate(reply1Hash)) / 3.0f;
		return (super.getWinRate(board, move) + global[color].getWinRate(globalHash)) / 2.0f;
	}

}
