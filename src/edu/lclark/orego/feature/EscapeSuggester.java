package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;

/**
 * Returns a set of moves that will allow groups to escape from atari by running
 * or merging, not by capturing.
 */
public final class EscapeSuggester implements Suggester {

	private final Board board;

	private final AtariObserver atariObserver;

	private final CoordinateSystem coords;

	/**
	 * A list of all of the moves for the current player to play that will allow
	 * a group to escape from atari
	 */
	private final ShortSet movesToEscape;

	/**
	 * Keeps track of the liberties of a chain that is possible to merge with.
	 */
	private final ShortSet tempLiberties;

	public EscapeSuggester(Board board, AtariObserver atariObserver) {
		this.board = board;
		coords = board.getCoordinateSystem();
		this.atariObserver = atariObserver;
		int n = coords.getFirstPointBeyondBoard();
		tempLiberties = new ShortSet(n);
		movesToEscape = new ShortSet(n);
	}

	@Override
	public ShortSet getMoves() {
		movesToEscape.clear();
		StoneColor colorToPlay = board.getColorToPlay();
		ShortSet chainsInAtari = atariObserver.getChainsInAtari(colorToPlay);
		for (int i = 0; i < chainsInAtari.size(); i++) {
			short chain = chainsInAtari.get(i);
			short p = board.getLiberties(chain).get(0);
			if (board.getNeighborsOfColor(p, VACANT) >= 2) {
				movesToEscape.add(p);
			} else if (board.getNeighborsOfColor(p, colorToPlay) > 0) {
				escapeByMerging(p);
			}
			escapeByCapturing(chain);
		}
		return movesToEscape;
	}

	/**
	 * Finds moves to escape from atari by capturing outside enemy stones. Any
	 * such moves are added to movesToEscape.
	 * 
	 * @param chain
	 *            The friendly chain in atari.
	 */
	private void escapeByCapturing(short chain) {
		StoneColor enemy = board.getColorToPlay().opposite();
		short p = chain;
		do {
			short[] neighbors = coords.getNeighbors(p);
			ShortSet enemiesInAtari = atariObserver.getChainsInAtari(enemy);
			for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
				short n = neighbors[i];
				Color color = board.getColorAt(n);
				if (color == enemy) {
					if (enemiesInAtari.contains(board.getChainRoot(n))) {
						movesToEscape.add(board.getLiberties(n).get(0));
					}
				}
			}			
			p = board.getChainNextPoint(p);
		} while (p != chain);
	}

	/**
	 * Finds moves to escape atari by merging with other chains. Any such moves
	 * are added to movesToEscape.
	 * 
	 * @param liberty
	 *            The liberty of the current chain in atari.
	 */
	private void escapeByMerging(short liberty) {
		tempLiberties.clear();
		short[] neighbors = coords.getNeighbors(liberty);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			if (board.getColorAt(n) == VACANT) {
				tempLiberties.add(n);
			} else if (board.getColorAt(n) == board.getColorToPlay()) {
				ShortSet neighborsLiberties = board.getLiberties(n);
				if (neighborsLiberties.size() > 1) {
					for (int j = 0; j < neighborsLiberties.size(); j++) {
						tempLiberties.add(neighborsLiberties.get(j));
						// 3 because there need to be 2 left not counting
						// liberty itself
						if (tempLiberties.size() == 3) {
							movesToEscape.add(liberty);
							return;
						}
					}
				}
			}
		}
	}

}
