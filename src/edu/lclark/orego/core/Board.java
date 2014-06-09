package edu.lclark.orego.core;

import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.Legality.*;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import edu.lclark.orego.feature.BoardObserver;
import edu.lclark.orego.util.ShortSet;
import edu.lclark.orego.util.ShortList;
import static edu.lclark.orego.core.Point.*;

public final class Board {

	/** Stones captured by the last move. */
	private final ShortList capturedStones;

	/** The color to play next. */
	private StoneColor colorToPlay;

	/** Coordinate system based on board width. */
	private final CoordinateSystem coords;

	/**
	 * Identifiers of enemy chains adjacent to the move just played. Used by
	 * isSuicidal() and isSelfAtari().
	 */
	private final ShortList enemyNeighboringChainIds;

	/** Identifiers of friendly chains adjacent to the move just played. */
	private final ShortList friendlyNeighboringChainIds;

	/**
	 * Zobrist hash of the current board position.
	 * 
	 * @see #getHash()
	 */
	private long hash;

	/** The point, if any, where the simple ko rule prohibits play. */
	private short koPoint;

	/** Direct liberties of the stone just played. */
	private final ShortSet lastPlayLiberties;

	/** Neighbors of a stone just captured. Used by removeStone(). */
	private final ShortList neighborsOfCapturedStone;

	/** Observers of this board. */
	private BoardObserver[] observers;

	/** Number of consecutive passes just played. */
	private short passes;

	/** Point on the board (and surrounding sentinels). */
	private final Point[] points;

	/** Hash after removing captured stones. */
	private long proposedHash;

	/**
	 * A hash table of all previous board positions for ko verification. The
	 * hash codes stored here do NOT include the simple ko point.
	 */
	private final SuperKoTable superKoTable;

	/** @see #getTurn() */
	private short turn;

	/** The set of vacant points. */
	private final ShortSet vacantPoints;
	
	public Board(int width) {
		coords = CoordinateSystem.forWidth(width);
		points = new Point[coords.getFirstPointBeyondExtendedBoard()];
		friendlyNeighboringChainIds = new ShortList(4);
		enemyNeighboringChainIds = new ShortList(4);
		capturedStones = new ShortList(coords.getArea());
		int n = coords.getFirstPointBeyondBoard();
		lastPlayLiberties = new ShortSet(n);
		superKoTable = new SuperKoTable(coords);
		vacantPoints = new ShortSet(n);
		for (short p = 0; p < points.length; p++) {
			points[p] = new Point(coords, p);
		}
		neighborsOfCapturedStone = new ShortList(4);
		observers = new BoardObserver[0];
		clear();
	}

	/**
	 * Adds an observer to this board.
	 */
	public void addObserver(BoardObserver observer) {
		// The assertions check that nothing has happened on this board
		assert hash == SuperKoTable.EMPTY;
		assert turn == 0;
		// Defensive copy
		observers = java.util.Arrays.copyOf(observers, observers.length + 1);
		observers[observers.length - 1] = observer;
	}

	/**
	 * Deals with enemy chains adjacent to the move just played at p, either
	 * capturing them or decrementing their liberty counts.
	 */
	private void adjustEnemyNeighbors(StoneColor color, short p) {
		capturedStones.clear();
		for (int i = 0; i < enemyNeighboringChainIds.size(); i++) {
			short enemy = enemyNeighboringChainIds.get(i);
			if (points[enemy].isInAtari()) {
				short s = enemy;
				do {
					removeStone(s);
					s = points[s].chainNextPoint;
				} while (s != enemy);
			} else {
				points[enemy].liberties.removeKnownPresent(p);
			}
		}
	}

	/**
	 * Deals with friendly neighbors of the move p just played, merging chains
	 * as necessary.
	 */
	private void adjustFriendlyNeighbors(StoneColor color, short p) {
		if (friendlyNeighboringChainIds.size() == 0) {
			// If there are no friendly neighbors, create a new, one-stone chain
			points[p].becomeOneStoneChain(lastPlayLiberties);
		} else {
			short c = friendlyNeighboringChainIds.get(0);
			points[p].addToChain(points[c]);
			points[c].liberties.addAll(lastPlayLiberties);
			if (friendlyNeighboringChainIds.size() > 1) {
				// If there are several friendly neighbors, merge them
				for (int i = 1; i < friendlyNeighboringChainIds.size(); i++) {
					short ally = friendlyNeighboringChainIds.get(i);
					if (points[c].liberties.size() >= points[ally].liberties
							.size()) {
						mergeChains(c, ally);
					} else {
						mergeChains(ally, c);
						c = ally;
					}
					
				}
			}
			points[c].liberties.removeKnownPresent(p);
		}
	}

	/**
	 * Returns this board to its blank state. Any initial stones are removed and
	 * the komi is reset to a default value. This is roughly equivalent to
	 * creating a new instance, but (a) it is faster, and (b) references to the
	 * board do not have to change.
	 */
	public void clear() {
		colorToPlay = BLACK;
		hash = SuperKoTable.EMPTY;
		koPoint = NO_POINT;
		passes = 0;
		superKoTable.clear();
		turn = 0;
		vacantPoints.clear();
		for (short p : coords.getAllPointsOnBoard()) {
			points[p].clear();
			vacantPoints.addKnownAbsent(p);
			int edgeCount = 0;
			short[] neighbors = coords.getNeighbors(p);
			for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
				short n = neighbors[i];
				if (!coords.isOnBoard(n)) {
					edgeCount++;
				}
			}
			points[p].neighborCounts += edgeCount * EDGE_INCREMENT;
		}
		for (BoardObserver observer : observers) {
			observer.clear();
		}
	}

	/**
	 * Copies data from that to this.
	 */
	public void copyDataFrom(Board that) {
		colorToPlay = that.colorToPlay;
		hash = that.hash;
		koPoint = that.koPoint;
		for (int i = 0; i < observers.length; i++) {
			observers[i].copyDataFrom(that.observers[i]);
		}
		passes = that.passes;
		for (short p : coords.getAllPointsOnBoard()) {
			points[p].copyDataFrom(that.points[p]);
		}
		superKoTable.copyDataFrom(that.superKoTable);
		turn = that.turn;
		vacantPoints.copyDataFrom(that.vacantPoints);
	}

	/**
	 * Updates data structures at the end of a play.
	 * 
	 * @param color
	 *            The color of the stone played.
	 * @param p
	 *            The location where the stone was played.
	 */
	private void finalizePlay(StoneColor color, short p) {
		int lastVacantPointCount = vacantPoints.size();
		points[p].color = color;
		vacantPoints.remove(p);
		boolean surrounded = points[p].hasMaxNeighborsForColor(color.opposite());
		short[] neighbors = coords.getNeighbors(p);
		for(int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++){
			points[neighbors[i]].neighborCounts += Point.NEIGHBOR_INCREMENT[color.index()];
		}
		adjustFriendlyNeighbors(color, p);
		adjustEnemyNeighbors(color, p);
		if ((lastVacantPointCount == vacantPoints.size()) & surrounded) {
			koPoint = vacantPoints.get((short) (vacantPoints.size() - 1));
		} else {
			koPoint = NO_POINT;
		}
	}
	
	/** Return the root of the chain that contains p. */
	public short getChainRoot(short p){
		return points[p].chainId;
	}

	/** Returns the color at point p. */
	public Color getColorAt(short p) {
		return points[p].color;
	}

	/** Returns the color to play next. */
	public StoneColor getColorToPlay() {
		return colorToPlay;
	}

	/** Returns the CoordinateSystem associated with this board. */
	public CoordinateSystem getCoordinateSystem() {
		return coords;
	}

	/**
	 * Returns the Zobrist hash of the current board position.
	 */
	public long getHash() {
		return hash;
	}
	
	/**
	 * Returns the liberties of p.
	 */
	public ShortSet getLiberties(short p) {
		return points[points[p].chainId].liberties;
	}
	
	/** Gets the neighbors of a given color, based on the int neighborCounts in Point. */
	public int getNeighborCount(Color color, short p){
		return points[p].getNeighborCount(color);
	}

	/**
	 * Returns the number of consecutive passes ending the move sequence so far.
	 */
	public int getPasses() {
		return passes;
	}

	/**
	 * Returns the current turn number (0 at the beginning of the game).
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * Returns the set of vacant points on this board.
	 */
	public ShortSet getVacantPoints() {
		return vacantPoints;
	}

	/**
	 * Returns the hash value that would result if the captured stones were
	 * removed. Used by play() to detect superko.
	 * 
	 * @param color
	 *            Color of the stone to be played.
	 * @param p
	 *            Location of the stone to be played.
	 */
	private long hashAfterRemovingCapturedStones(StoneColor color, short p) {
		long result = hash;
		result ^= coords.getHash(color, p);
		StoneColor enemy = color.opposite();
		for (int i = 0; i < enemyNeighboringChainIds.size(); i++) {
			short c = enemyNeighboringChainIds.get(i);
			if (points[c].isInAtari()) {
				short active = c;
				do {
					result ^= coords.getHash(enemy, active);
					active = points[active].chainNextPoint;
				} while (active != c);
			}
		}
		return result;
	}

	/**
	 * Returns true if the stone at p has the maximum possible number of
	 * neighbors of color p.
	 */
	public boolean hasMaxNeighborsForColor(StoneColor color, short p) {
		return points[p].hasMaxNeighborsForColor(color);
	}

	/**
	 * Returns true if p (which might be a point on the board or PASS) is legal.
	 */
	public boolean isLegal(short p) {
		if (p == PASS) {
			return true;
		}
		return legality(colorToPlay, p) == OK;
	}

	/**
	 * Visits neighbors of p, looking for potential captures and chains to merge
	 * with the new stone. As a side effect, loads the fields
	 * friendlyNeighboringChainIds, enemyNeighboringChainIds, and
	 * lastPlayLiberties, used by finalizePlay.
	 * 
	 * @return true if playing at p would be suicidal.
	 */
	private boolean isSuicidal(StoneColor color, short p) {
		friendlyNeighboringChainIds.clear();
		enemyNeighboringChainIds.clear();
		lastPlayLiberties.clear();
		boolean suicide = true;
		short[] neighbors = coords.getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			Color neighborColor = points[n].color;
			if (neighborColor == VACANT) { // Vacant point
				lastPlayLiberties.add(n);
				suicide = false;
			} else if (neighborColor == color) { // Friendly neighbor
				short chainId = points[n].chainId;
				friendlyNeighboringChainIds.addIfNotPresent(chainId);
				suicide &= points[chainId].isInAtari();
			} else if (neighborColor != OFF_BOARD) { // Enemy neighbor
				short chainId = points[n].chainId;
				enemyNeighboringChainIds.addIfNotPresent(chainId);
				suicide &= !(points[chainId].isInAtari());
			}
		}
		return suicide;
	}

	/**
	 * Returns the legality of playing at p. As a side effect, loads the fields
	 * friendlyNeighboringChainIds, enemyNeighboringChainIds, and
	 * lastPlayLiberties, used by finalizePlay.
	 */
	private Legality legality(StoneColor color, short p) {
		assert coords.isOnBoard(p);
		if (turn >= coords.getMaxMovesPerGame() - 2) {
			return GAME_TOO_LONG;
		}
		if (points[p].color != VACANT) {
			return OCCUPIED;
		}
		if (p == koPoint) {
			return KO_VIOLATION;
		}
		if (isSuicidal(color, p)) {
			return SUICIDE;
		}
		proposedHash = hashAfterRemovingCapturedStones(color, p);
		if (superKoTable.contains(proposedHash)) {
			return KO_VIOLATION;
		}
		return OK;
	}

	/**
	 * Similar to #legality, but doesn't check for occupied point or superko
	 * violation.
	 */
	private Legality legalityFast(StoneColor color, short p) {
		assert coords.isOnBoard(p);
		if (turn >= coords.getMaxMovesPerGame() - 2) {
			return GAME_TOO_LONG;
		}
		if (p == koPoint) {
			return KO_VIOLATION;
		}
		if (isSuicidal(color, p)) {
			return SUICIDE;
		}
		return OK;
	}

	/**
	 * Merges the stones in appendage into the chain at base. Each parameter is
	 * a stone in one of the chains to be merged.
	 * 
	 * @param base
	 *            If not too expensive to compute, base should be the larger of
	 *            the two chains.
	 */
	private void mergeChains(short base, short appendage) {
		points[base].liberties.addAll(points[appendage].liberties);
		int active = appendage;
		do {
			points[active].chainId = points[base].chainId;
			active = points[active].chainNextPoint;
		} while (active != appendage);
		short temp = points[base].chainNextPoint;
		points[base].chainNextPoint = points[appendage].chainNextPoint;
		points[appendage].chainNextPoint = temp;
	}

	/** Notify the observers about what has changed. */
	private void notifyObservers(StoneColor color, short p) {
		for (BoardObserver observer : observers) {
			// Note that we have to flip colorToPlay because it has already been
			// flipped as the move was completed
			observer.update(color, p, capturedStones);
		}
	}

	/** Plays a pass move. */
	public void pass() {
		if (koPoint != NO_POINT) {
			koPoint = NO_POINT;
		}
		colorToPlay = colorToPlay.opposite();
		passes++;
		turn++;
		notifyObservers(colorToPlay.opposite(), PASS);
	}

	/** Places a stone of color at point p. */
	private void placeInitialStone(StoneColor color, short p) {
		// Initial stones will always be legal, but the legality method
		// also sets up some fields called by finalizePlay.
		legality(color, p);
		finalizePlay(color, p);
		hash = proposedHash;
		superKoTable.add(hash);
		// To ensure that the board is in a stable state, this must be done last
		notifyObservers(color, p);
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
		passes = 0;
		turn++;
		hash = proposedHash;
		superKoTable.add(hash);
		// To ensure that the board is in a stable state, this must be done last
		// The color argument is flipped back to the color of the stone played
		notifyObservers(colorToPlay.opposite(), p);
		return OK;
	}

	/**
	 * Similar to play, but assumes p is on board and not occupied. Does not
	 * maintain hash or check superko.
	 */
	public Legality playFast(short p) {
		Legality result = legalityFast(colorToPlay, p);
		if (result != OK) {
			return result;
		}
		finalizePlay(colorToPlay, p);
		colorToPlay = colorToPlay.opposite();
		passes = 0;
		turn++;
		// To ensure that the board is in a stable state, this must be done last
		// The color argument is flipped back to the color of the stone played
		notifyObservers(colorToPlay.opposite(), p);
		return OK;
	}

	/** Removes the stone at p. */
	private void removeStone(short p) {
		points[p].color = VACANT;
		vacantPoints.addKnownAbsent(p);
		neighborsOfCapturedStone.clear();
		short[] neighbors = coords.getNeighbors(p);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			points[n].neighborCounts -= Point.NEIGHBOR_INCREMENT[colorToPlay.opposite().index()];
			if (points[n].color == BLACK | points[n].color == WHITE) {
				neighborsOfCapturedStone.addIfNotPresent(points[n].chainId);
			}
		}
		for (int k = 0; k < neighborsOfCapturedStone.size(); k++) {
			int c = neighborsOfCapturedStone.get(k);
			points[c].liberties.addKnownAbsent(p);
		}
		capturedStones.add(p);
	}

	/**
	 * Places all of the stones indicated in diagram. These are set as initial
	 * stones, not moves recorded in the board's history. The color to play next
	 * is set as indicated.
	 * @throws IllegalArgumentException if the diagram contains invalid characters
	 */
	public void setUpProblem(String[] diagram, StoneColor colorToPlay) {
		assert diagram.length == coords.getWidth();
		clear();
		for (int r = 0; r < coords.getWidth(); r++) {
			assert diagram[r].length() == coords.getWidth();
			for (int c = 0; c < coords.getWidth(); c++) {
				StoneColor color = StoneColor.forChar(diagram[r].charAt(c));
				if (color != null) {
					placeInitialStone(color, coords.at(r, c));
				} else if (NonStoneColor.forChar(diagram[r].charAt(c)) != VACANT) {
					throw new IllegalArgumentException();
				}
			}
		}
		this.colorToPlay = colorToPlay;
	}

	@Override
	public String toString() {
		String result = "";
		for (int r = 0; r < coords.getWidth(); r++) {
			for (int c = 0; c < coords.getWidth(); c++) {
				result += points[coords.at(r, c)].color.toChar();
			}
			result += "\n";
		}
		return result;
	}

}
