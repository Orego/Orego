package orego.shape;

import static orego.core.Board.MAX_MOVES_PER_GAME;
import static orego.core.Coordinates.*;
import orego.core.SuperKoTable;

/**
 * Set of Zobrist hashes (longs) from previous board positions. This is a hash
 * table, but without all of the overhead of java.util.HashSet. It only supports
 * insertion, search, and copying. Collisions are resolved by linear probing.
 */
public class MoveHashTable {

	/** Special value for an empty slot in the table. */
	public static final long EMPTY = 0;

	/**
	 * Bit mask to make hash codes positive. Math.abs() won't work because
	 * abs(Integer.minValue()) < 0.
	 */
	public static final int IGNORE_SIGN_BIT = 0x7fffffff;

	/**
	 * True if the special value 0 has been stored. Because this value is used
	 * to mark an empty slot, we can't look it up in the usual way. Instead, we
	 * just set this flags.
	 */
	private int emptyBoardMove;

	/** The table proper. */
	private long[] data;
	private int[] move;

	public MoveHashTable() {
		data = new long[65536]; // This is 2^16.
		move = new int[65536];
		emptyBoardMove = NO_POINT;
	}

	/** Adds key to this table. */
	public void add(long key, int move) {
		if (key == 0) {
			emptyBoardMove = move;
		} else {
			int slot = (((int) key) & IGNORE_SIGN_BIT) % data.length;
			while (data[slot] != EMPTY) {
				if (data[slot] == key) {
					this.move[slot] = move;
					return;
				}
				slot = (slot + 1) % data.length;
			}
			data[slot] = key;
			this.move[slot] = move;
		}
	}

	/** Returns true if key is in this table. */
	public int getMove(long key) {
		if (key == 0) {
			return emptyBoardMove;
		} else {
			int slot = (((int) key) & IGNORE_SIGN_BIT) % data.length;
			while (data[slot] != EMPTY) {
				if (data[slot] == key) {
					return move[slot];
				}
				slot = (slot + 1) % data.length;
			}
			return NO_POINT;
		}
	}

	/**
	 * Makes this into a copy of that, without the overhead of creating a new
	 * object.
	 */
	public void copyDataFrom(MoveHashTable that) {
		System.arraycopy(that.data, 0, data, 0, data.length);
		System.arraycopy(that.move, 0, move, 0, move.length);
		emptyBoardMove = that.emptyBoardMove;
	}

}
