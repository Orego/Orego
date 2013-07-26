package orego.shape;

import java.io.Serializable;

import orego.core.*;
import static orego.core.Board.MAX_PATTERN_RADIUS;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.core.Colors.WHITE;

/** A cluster of tables, allowing for multiple radii and separate patterns for each color. */
public class Cluster implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** Multihash tables, indexed by radius and color to play. */
	private Table[][] tables;

	/**
	 * @param tables Number of sloppy hash tables.
	 * @param bits Number of bits for indexing into each table.
	 */
	public Cluster(int tables, int bits) {
		this.tables = new Table[MAX_PATTERN_RADIUS + 1][NUMBER_OF_PLAYER_COLORS];
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			for (int color = BLACK; color <= WHITE; color++) {
				this.tables[radius][color] = new Table(tables, bits);
			}
		}
	}
	
	/** Set every table entry to have a specified number of runs. */
	public void setCount(long count) {
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			for (int color = BLACK; color <= WHITE; color++) {
				tables[radius][color].setCount(count);
			}
		}
	}

	/**
	 * @param board Board on which move is made.
	 * @param move Move to be made.
	 * @param win 1 or 0.
	 */
	public void store(Board board, int move, int win) {
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			tables[radius][board.getColorToPlay()].store(
					board.getPatternHash(move, radius), win);
		}
	}
	
	/**
	 * @param board Board on which move is made.
	 * @param move Move to be made.
	 * @param win 1 or 0.
	 * @param radius The radius of the pattern being stored
	 */
	public void store(Board board, int move, int win, int radius) {
		tables[radius][board.getColorToPlay()].store(
					board.getPatternHash(move, radius), win);
	}

	/** Returns the win rate for playing move on board. */
	public float getWinRate(Board board, int move) {
		float sum = 0.0f;
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			sum += (5 - radius) * tables[radius][board.getColorToPlay()].getWinRate(board.getPatternHash(move, radius));
		}
		return sum / 10;
//		return sum / MAX_PATTERN_RADIUS;
	}
	
	/** Returns the run count for playing move on board. */
	public long getCount(Board board, int move) {
		long sum = 0L;
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			sum += tables[radius][board.getColorToPlay()].getCount(board.getPatternHash(move, radius));
		}
		return sum / MAX_PATTERN_RADIUS;
	}
	
	/** Returns the win rate for a particular pattern hash. */
	public float getPatternWinRate(long hash, int color, int radius){
		return tables[radius][color].getWinRate(hash);
	}
	
	/** Returns the count for a particular pattern hash. */
	public long getPatternCount(long hash, int color, int radius){
		return tables[radius][color].getCount(hash);
	}

	/** Returns the win rate for playing move on board using only patterns of a given radius. */
	public float getWinRate(Board board, int move, int radius) {
		return tables[radius][board.getColorToPlay()].getWinRate(board.getPatternHash(move, radius));
	}

	/**
	 * Stores a win (or loss) for a color at the indicated hashes (indexed by radius).
	 * @param win 1 for a win, 0 for a loss.
	 */
	public void store(long[] hashes, int color, int win) {
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			tables[radius][color].store(hashes[radius], win);
		}		
	}
	
}
