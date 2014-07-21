package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.FIRST_ORTHOGONAL_NEIGHBOR;
import static edu.lclark.orego.core.CoordinateSystem.LAST_ORTHOGONAL_NEIGHBOR;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.ShortSet;

/**
 * Returns a set of moves that will allow groups to escape from atari by
 * running, merging, or capturing. Does not avoid snapbacks.
 */
@SuppressWarnings("serial")
public final class EscapeSuggester implements Suggester {

	private final AtariObserver atariObserver;

	private final int bias;

	private final Board board;
	
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
	
	public EscapeSuggester(Board board, AtariObserver atariObserver){
		this(board, atariObserver, 0);
	}

	public EscapeSuggester(Board board, AtariObserver atariObserver, int bias) {
		this.bias = bias;
		this.board = board;
		coords = board.getCoordinateSystem();
		this.atariObserver = atariObserver;
		final int n = coords.getFirstPointBeyondBoard();
		tempLiberties = new ShortSet(n);
		movesToEscape = new ShortSet(n);
	}

	/**
	 * Finds moves allowing chain to escape from atari by capturing outside
	 * enemy stones. Does not avoid snapbacks. Any such moves are added to
	 * movesToEscape.
	 *
	 * @param chain
	 *            The friendly chain in atari.
	 */
	private void escapeByCapturing(short chain) {
		final StoneColor enemy = board.getColorToPlay().opposite();
		short p = chain;
		do {
			final short[] neighbors = coords.getNeighbors(p);
			final ShortSet enemiesInAtari = atariObserver.getChainsInAtari(enemy);
			for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
				final short n = neighbors[i];
				final Color color = board.getColorAt(n);
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
		final short[] neighbors = coords.getNeighbors(liberty);
		for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
			final short n = neighbors[i];
			if (board.getColorAt(n) == VACANT) {
				tempLiberties.add(n);
			} else if (board.getColorAt(n) == board.getColorToPlay()) {
				final ShortSet neighborsLiberties = board.getLiberties(n);
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

	@Override
	public int getBias() {
		return bias;
	}

	@Override
	public ShortSet getMoves() {
		movesToEscape.clear();
		final StoneColor colorToPlay = board.getColorToPlay();
		final ShortSet chainsInAtari = atariObserver.getChainsInAtari(colorToPlay);
		for (int i = 0; i < chainsInAtari.size(); i++) {
			final short chain = chainsInAtari.get(i);
			final short p = board.getLiberties(chain).get(0);
			if (board.getNeighborsOfColor(p, VACANT) >= 2) {
				movesToEscape.add(p);
			} else if (board.getNeighborsOfColor(p, colorToPlay) > 0) {
				escapeByMerging(p);
			}
			escapeByCapturing(chain);
		}
		return movesToEscape;
	}

}
