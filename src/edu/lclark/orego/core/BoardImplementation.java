package edu.lclark.orego.core;

import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static java.util.Arrays.*;
import edu.lclark.orego.util.ShortSet;
import orego.util.IntList;

public final class BoardImplementation {

	/** The liberties of each chain. */
	private ShortSet[] liberties;

	/** Liberties of the stone just played. */
	private ShortSet lastPlayLiberties;

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

	/** Neighbors of a stone just captured. Used by removeStone(). */
	private IntList neighborsOfCapturedStone;
	
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
		lastPlayLiberties = new ShortSet(n);
		liberties = new ShortSet[n];
		for (short p : coordinateSystem.getAllPointsOnBoard()) {
			liberties[p] = new ShortSet(n);
		}
		neighborsOfCapturedStone = new IntList(4);
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
			liberties[p].clear();
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
		System.out.println("Placing initial " + color + " stone at " + coordinateSystem.pointToString(p));
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

	/** Returns true if the stone at p has the maximum possible number of neighbors of color p. */
	private boolean hasMaxNeighborsForColor(StoneColor color, short p) {
		short[] neighbors = getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			Color c = colors[neighbors[i]];
			if (c != color && c != OFF_BOARD) {
				return false;
			}
		}
		return true;
	}

	/** Removes the stone at p. */
	public void removeStone(short p) {
		StoneColor color = (StoneColor)(colors[p]);
//		stoneCounts[color]--;
//		hash ^= ZOBRIST_HASHES[color][p];
		System.out.println("Removing " + colors[p] + " stone at " + coordinateSystem.pointToString(p));
		colors[p] = VACANT;
//		vacantPoints.addKnownAbsent(p);
		neighborsOfCapturedStone.clear();
		short[] neighbors = getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
//			neighborCounts[n] -= NEIGHBOR_INCREMENT[color];
			// This seems to be a rare appropriate use of instanceof
			if (colors[n] instanceof StoneColor) {
				neighborsOfCapturedStone.addIfNotPresent(chainIds[n]);
			}
		}
		StoneColor enemyColor = color.opposite();
		for (int k = 0; k < neighborsOfCapturedStone.size(); k++) {
			int c = neighborsOfCapturedStone.get(k);
			liberties[c].addKnownAbsent(p);
//			if (liberties[c].size() > 1) {
//				chainsInAtari[enemyColor].remove(c);
//			}
		}
		// Clear chainId so that a stone removed by undo() doesn't appear to
		// still be part of its former chain
		// (This would be a problem when updating liberties if the same move
		// were replayed.)
		chainIds[p] = p;
	}

	/**
	 * Deals with enemy chains adjacent to the move just played at p, either
	 * capturing them or decrementing their liberty counts.
	 * 
	 * @param Color The color of the stone just played.
	 */
	private void adjustEnemyNeighbors(StoneColor color, short p) {
		System.out.println("Adjusting enemy neigbors around " + color + " stone at " + coordinateSystem.pointToString(p));
		// TODO Should the caller find the opposite color?
		StoneColor enemyColor = color.opposite();
		for (int i = 0; i < enemyNeighboringChainIds.size(); i++) {
			System.out.println(coordinateSystem.pointToString((short)(enemyNeighboringChainIds.get(i))));
			// TODO Remove cast
			short enemy = (short)(enemyNeighboringChainIds.get(i));
			System.out.println("Enemy chain at " + coordinateSystem.pointToString(enemy));
			if (liberties[enemy].size() == 1) {
				System.out.println("Captured something");
//				chainsInAtari[enemyColor].remove(enemy);
				short s = enemy;
				do {
					System.out.println("Removing stone at " + coordinateSystem.pointToString(s));
					removeStone(s);
					s = chainNextPoints[s];
				} while (s != enemy);
			} else {
				liberties[enemy].removeKnownPresent(p);
//				if (liberties[enemy].size() == 1) {
//					chainsInAtari[enemyColor].addKnownAbsent(enemy);
//				}
			}
		}
	}

	/**
	 * Updates data structures at the end of a play.
	 * 
	 * @param color The color of the stone played.
	 * @param p The location where the stone was played.
	 */
	private void finalizePlay(StoneColor color, short p) {
//		int lastVacantPointCount = vacantPoints.size();
		System.out.println("Adding " + color + " stone at " + coordinateSystem.pointToString(p));
		colors[p] = color;
		// TODO Update stone counts, hash, vacant points, maybe neighbor counts
		boolean surrounded = hasMaxNeighborsForColor(color.opposite(), p);
		
		adjustFriendlyNeighbors(color, p);
		adjustEnemyNeighbors(color, p);
		
//		if (liberties[chainIds[p]].size() == 1) {
//			chainsInAtari[color].add(chainIds[p]);
//		}
		// The rest is about the local ko point and hash
//		hash ^= ZOBRIST_HASHES[VACANT][koPoint];
//		if ((lastVacantPointCount == vacantPoints.size()) & surrounded) {
//			koPoint = vacantPoints.get(vacantPoints.size() - 1);
//		} else {
//			koPoint = NO_POINT;
//		}
//		hash ^= ZOBRIST_HASHES[VACANT][koPoint];
	}

	/** Adds stone p to chain. */
	private void addStone(short p, short chain) {
		chainNextPoints[p] = chainNextPoints[chain];
		chainNextPoints[chain] = p;
		chainIds[p] = chain;
	}

	// TODO The casts to short will be unnecessary once this is a ShortList instead of an IntList
	/**
	 * Deals with friendly neighbors of the move p just played, merging chains
	 * as necessary.
	 */
	private void adjustFriendlyNeighbors(StoneColor color, short p) {
		if (friendlyNeighboringChainIds.size() == 0) {
			// If there are no friendly neighbors, create a new, one-stone chain
			chainNextPoints[p] = p;
			liberties[p].copyDataFrom(lastPlayLiberties);
//			if (liberties[p].size() == 1) {
//				chainsInAtari[color].addKnownAbsent(p);
//			}
		} else if (friendlyNeighboringChainIds.size() == 1) {
			// If there is only one friendly neighbor, add this stone to that
			// chain
			short c = (short)(friendlyNeighboringChainIds.get(0));
			liberties[c].addAll(lastPlayLiberties);
			liberties[c].removeKnownPresent(p);
			addStone(p, c);
//			if (liberties[c].size() == 1) {
//				chainsInAtari[color].add(c);
//			} else {
//				chainsInAtari[color].remove(c);
//			}
		} else {
			// If there are several friendly neighbors, merge them
			short c = (short)(friendlyNeighboringChainIds.get(0));
			addStone(p, c);
			for (int i = 1; i < friendlyNeighboringChainIds.size(); i++) {
				short ally = (short)(friendlyNeighboringChainIds.get(i));
				if (liberties[ally].size() <= liberties[c].size()) {
					mergeChains(c, ally);
				} else {
					mergeChains(ally, c);
					c = ally;
				}
			}
			liberties[c].addAll(lastPlayLiberties);
			assert liberties[c].contains(p);
			liberties[c].removeKnownPresent(p);
//			if (liberties[c].size() == 1) {
//				chainsInAtari[color].add(c);
//			} else {
//				chainsInAtari[color].remove(c);
//			}
		}
	}

	/**
	 * Merges the stones in appendage into the chain at base. Each parameter is
	 * a stone in one of the chains to be merged.
	 * 
	 * @param base
	 *            if not too expensive to compute, should be the larger of the
	 *            two chains.
	 */
	private void mergeChains(int base, int appendage) {
		liberties[base].addAll(liberties[appendage]);
//		chainsInAtari[colors[appendage]].remove(appendage);
		int active = appendage;
		do {
			chainIds[active] = chainIds[base];
			active = chainNextPoints[active];
		} while (active != appendage);
		short temp = chainNextPoints[base];
		chainNextPoints[base] = chainNextPoints[appendage];
		chainNextPoints[appendage] = temp;
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
