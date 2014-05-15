package edu.lclark.orego.core;

import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static java.util.Arrays.*;
import static orego.core.Coordinates.getFirstPointBeyondBoard;
import orego.util.IntList;
import orego.util.IntSet;

public final class BoardImplementation {

	/** The liberties of each chain. */
	private IntSet[] liberties;

	/** Liberties of the stone just played. */
	private IntSet lastPlayLiberties;

	/**
	 * Identifiers of enemy chains adjacent to the move just played. Used by
	 * isSuicidal() and isSelfAtari().
	 */
	private IntList enemyNeighboringChainIds;

	/** Identifiers of friendly chains adjacent to the move just played. */
	private IntList friendlyNeighboringChainIds;

	/** Colors of points on the board. */
	private final Color[] colors;

	/**
	 * Identifier of chain for each point (location of the "root" stone in that
	 * chain). For vacant points, this is the point itself.
	 */
	private final short[] chainIds;

	/**
	 * Next "pointers" for each occupied point, linking points into chains.
	 */
	private final short[] chainNextPoints;

	/** The color to play next. */
	private StoneColor colorToPlay;

	/** Coordinate system based on board width. */
	private final CoordinateSystem coordinateSystem;

	public BoardImplementation(int width) {
		coordinateSystem = CoordinateSystem.forWidth(width);
		// Many arrays are of these sizes, so naming them clarifies the code
		short n = coordinateSystem.getFirstPointBeyondBoard();
		short extended = coordinateSystem.getFirstPointBeyondExtendedBoard();
		colors = new Color[extended];
		chainIds = new short[extended];
		chainNextPoints = new short[n];
		friendlyNeighboringChainIds = new IntList(4);
		enemyNeighboringChainIds = new IntList(4);
		lastPlayLiberties = new IntSet(n);
		liberties = new IntSet[n];
		for (short p : coordinateSystem.getAllPointsOnBoard()) {
			liberties[p] = new IntSet(n);
		}
		clear();
	}

	/**
	 * @see edu.lclark.orego.core.CoordinateSystem#at(int, int)
	 */
	private short at(int r, int c) {
		return coordinateSystem.at(r, c);
	}

	/** @see CoordinateSystem#at(String) */
	public short at(String label) {
		return coordinateSystem.at(label);
	}

	/**
	 * Returns this board to its blank state. Any initial stones are removed and
	 * the komi is reset to a default value. This is roughly equivalent to
	 * creating a new instance, but (a) it is faster, and (b) references to the
	 * board do not have to change.
	 */
	public void clear() {
		fill(colors, OFF_BOARD);
		for (short p : coordinateSystem.getAllPointsOnBoard()) {
			colors[p] = VACANT;
			chainIds[p] = p;
		}
		colorToPlay = BLACK;
	}

	/** Returns the color at point p. */
	public Color getColorAt(short p) {
		return colors[p];
	}
	
	/** Returns the color to play next. */
	public StoneColor getColorToPlay() {
		return colorToPlay;
	}

	/**
	 * Returns an array of the neighbors of p.
	 * 
	 * @see CoordinateSystem#getNeighbors(short)
	 */
	public short[] getNeighbors(short p) {
		return coordinateSystem.getNeighbors(p);
	}

	/**
	 * @return
	 * @see edu.lclark.orego.core.CoordinateSystem#getWidth()
	 */
	public int getWidth() {
		return coordinateSystem.getWidth();
	}

	/** Plays a pass move. */
	public void pass() {
		// TODO Update ko point, hash, number of passes, history 
//		if (koPoint != NO_POINT) {
//			hash ^= ZOBRIST_HASHES[VACANT][koPoint];
//			koPoint = NO_POINT;
//		}
		colorToPlay = colorToPlay.opposite();
//		passes++;
//		moves[turn] = PASS;
//		turn++;
	}

	/** Places a stone of color at point p. */
	private void placeInitialStone(StoneColor color, short p) {
		// Initial stones will always be legal, but the legality method
		// also sets up some fields called by finalizePlay.
		legality(color, p);
		finalizePlay(color, p);
		// TODO Update hash code
	}

	/**
	 * Plays a move at point p if possible. Has no side effect if the move is
	 * illegal. Returns the legality of that move.
	 */
	public Legality play(short p) {
		if (p == PASS) {
			pass();
			return OK;
		}
		Legality result = legality(colorToPlay, p);
		if (result != OK) {
			return result;
		}
		finalizePlay(colorToPlay, p);
		colorToPlay = colorToPlay.opposite();
		// TODO Update passes, move history
//		passes = 0;
//		moves[turn] = p;
//		turn++;
		// TODO Update hash code, superko table
		// TODO This currently considers any move legal!
		return OK;
	}

	/**
	 * Updates data structures at the end of a play.
	 */
	private void finalizePlay(StoneColor color, short p) {
//		int lastVacantPointCount = vacantPoints.size();
		colors[p] = colorToPlay;
		// TODO Update stone counts, hash, vacant points, maybe neighbor counts
//		boolean surrounded = hasMaxNeighborsForColor(neighborCounts[p],
//				opposite(color));
		
//		adjustFriendlyNeighbors(color, p);
//		adjustEnemyNeighbors(color, p);
		
//		if (liberties[chainIds[p]].size() == 1) {
//			chainsInAtari[color].add(chainIds[p]);
//		}
		// The rest is about the local ko point
//		hash ^= ZOBRIST_HASHES[VACANT][koPoint];
//		if ((lastVacantPointCount == vacantPoints.size()) & surrounded) {
//			koPoint = vacantPoints.get(vacantPoints.size() - 1);
//		} else {
//			koPoint = NO_POINT;
//		}
//		hash ^= ZOBRIST_HASHES[VACANT][koPoint];
	}

	/**
	 * Visits neighbors of p, looking for potential captures and chains to merge
	 * with the new stone. As a side effect, loads the fields
	 * friendlyNeighboringChainIds, enemyNeighboringChainIds, and lastPlayLiberties.
	 * 
	 * @return true if playing at p would be suicidal.
	 */
	private boolean isSuicidal(StoneColor color, short p) {
		friendlyNeighboringChainIds.clear();
		enemyNeighboringChainIds.clear();
		lastPlayLiberties.clear();
		boolean suicide = true;
		short[] neighbors = getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			Color neighborColor = colors[n];
			if (neighborColor == VACANT) { // Vacant point
				lastPlayLiberties.add(n);
				suicide = false;
			} else if (neighborColor == color) { // Friendly neighbor
				int chainId = chainIds[n];
				friendlyNeighboringChainIds.addIfNotPresent(chainId);
				suicide &= (liberties[chainId].size() == 1);
			} else if (neighborColor != OFF_BOARD) { // Enemy neighbor
				int chainId = chainIds[n];
				enemyNeighboringChainIds.addIfNotPresent(chainId);
				suicide &= !(liberties[chainId].size() == 1);
			}
		}
		return suicide;
	}

	/** Returns the legality of playing at p. */
	public Legality legality(StoneColor color, short p) {
		assert coordinateSystem.isOnBoard(p);
		if (colors[p] != VACANT) {
			return OCCUPIED;
		}
		// TODO Game too long, simple ko
		if (isSuicidal(colorToPlay, p)) {
			return SUICIDE;
		}
		// TODO Superko
		return OK;
	}

	/**
	 * Places all of the stones indicated in diagram. These are set as initial
	 * stones, not moves recorded in the board's history. The color to play next
	 * is set as indicated.
	 */
	public void setUpProblem(String[] diagram, StoneColor colorToPlay) {
		assert diagram.length == getWidth();
		clear();
		for (int r = 0; r < getWidth(); r++) {
			assert diagram[r].length() == getWidth();
			for (int c = 0; c < getWidth(); c++) {
				StoneColor color = StoneColor.forChar(diagram[r].charAt(c));
				if (color != null) {
					placeInitialStone(color, at(r, c));
				}
			}
		}
		this.colorToPlay = colorToPlay;
		// TODO Put this new hash code in the superko table
	}

	@Override
	public String toString() {
		String result = "";
		for (int r = 0; r < getWidth(); r++) {
			for (int c = 0; c < getWidth(); c++) {
				result += colors[at(r, c)].toChar();
			}
			result += "\n";
		}
		return result;
	}

}
