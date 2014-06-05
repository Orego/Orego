package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import static edu.lclark.orego.core.CoordinateSystem.*;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.StoneColor;
import edu.lclark.orego.util.ShortList;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.StoneColor.*;
import static edu.lclark.orego.core.NonStoneColor.*;

/** Tracks all of the chains currently in atari for each color. */
public final class AtariObserver implements BoardObserver {

	private final Board board;

	private final CoordinateSystem coords;

	private final ShortSet[] chainsInAtari;

	private final ShortList itemsToRemove;

	public AtariObserver(Board board) {
		this.board = board;
		coords = board.getCoordinateSystem();
		board.addObserver(this);
		chainsInAtari = new ShortSet[] { new ShortSet(coords.getFirstPointBeyondBoard()),
				new ShortSet(coords.getFirstPointBeyondBoard()) };
		itemsToRemove = new ShortList(4);
	}

	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		if (location != PASS) {
			if (board.getLiberties(location).size() == 1) {
				chainsInAtari[color.index()].add(board.getChainRoot(location));
			}
			short[] neighbors = coords.getNeighbors(location);
			for (int i = FIRST_ORTHOGONAL_NEIGHBOR; i <= LAST_ORTHOGONAL_NEIGHBOR; i++) {
				short n = neighbors[i];
				if (board.getColorAt(n) == color.opposite()
						&& board.getLiberties(n).size() == 1) {
					chainsInAtari[color.opposite().index()].add(board
							.getChainRoot(n));
				}
			}
			removeInvalidChains(color);
			removeInvalidChains(color.opposite());
		}
	}

	/**
	 * Removes any chains in the atari lists that are either no longer chains or
	 * no longer in atari.
	 */
	private void removeInvalidChains(StoneColor color) {
		int index = color.index();
		itemsToRemove.clear();
		for (int i = 0; i < chainsInAtari[index].size(); i++) {
			short p = chainsInAtari[index].get(i);
			if (board.getColorAt(p) == VACANT || board.getChainRoot(p) != p) {
				itemsToRemove.add(p);
			} else {
				if (board.getLiberties(p).size() > 1) {
					itemsToRemove.add(p);
				}
			}
		}
		for (int i = 0; i < itemsToRemove.size(); i++) {
			chainsInAtari[index].remove(itemsToRemove.get(i));
		}
	}

	/** Returns the IDs of all the chains of a given color that are in atari. */
	public ShortSet getChainsInAtari(StoneColor color) {
		return chainsInAtari[color.index()];
	}

	@Override
	public void clear() {
		chainsInAtari[BLACK.index()].clear();
		chainsInAtari[WHITE.index()].clear();
	}

}
