package orego.shape;

import java.io.Serializable;

import orego.core.*;
import static orego.core.Board.MAX_PATTERN_RADIUS;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.core.Colors.WHITE;

/** A multistage filter, allowing for multiple radii and separate patterns for each color. */
public class MultiStageFilter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** Filters indexed by radius and color to play. */
	private Filter[][] filters;

	/**
	 * @param tables Number of sloppy hash tables.
	 * @param bits Number of bits for indexing into each table.
	 */
	public MultiStageFilter(int tables, int bits) {
		this.filters = new Filter[MAX_PATTERN_RADIUS + 1][NUMBER_OF_PLAYER_COLORS];
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			for (int color = BLACK; color <= WHITE; color++) {
				this.filters[radius][color] = new Filter(tables, bits);
			}
		}
	}

	/** Returns the lowest count for a particular pattern hash. */
	public long getPatternCount(long hash, int color, int radius){
		return filters[radius][color].getLowestCount(hash);
	}
	
	/** Returns whether this move is reasonable for any radius. */
	public boolean isReasonable(Board board, int move){
		return isReasonable(board,move,1,MAX_PATTERN_RADIUS);
	}
	
	/** Returns whether this move is reasonable for the specified radius. */
	public boolean isReasonable(Board board, int move, int radius){
		return filters[radius][board.getColorToPlay()].isReasonable(board.getPatternHash(move, radius));
	}
	
	/** Returns whether this move is reasonable for the specified range of radii. */
	public boolean isReasonable(Board board, int move, int minRadius, int maxRadius){
		int color = board.getColorToPlay();
		for (int radius = minRadius; radius <= maxRadius; radius++){
			if (filters[radius][color].isReasonable(board.getPatternHash(move, radius))){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Store run
	 * @param board Board on which move is made.
	 * @param move Move to be made.
	 */
	public void store(Board board, int move) {
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			filters[radius][board.getColorToPlay()].store(
					board.getPatternHash(move, radius));
		}
	}

	/**
	 * Stores a run for a color at the indicated hashes (indexed by radius).
	 */
	public void store(long[] hashes, int color) {
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			filters[radius][color].store(hashes[radius]);
		}		
	}
	
}
