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

	public AtariObserver(Board board) {
		this.board = board;
		coords = board.getCoordinateSystem();
		board.addObserver(this);
		chainsInAtari = new ShortSet[] { new ShortSet(coords.getFirstPointBeyondBoard()),
				new ShortSet(coords.getFirstPointBeyondBoard()) };
	}

	@Override
	public void update(StoneColor color, short location,
			ShortList capturedStones) {
		if (location != PASS) {
			removeInvalidChains(color);
			removeInvalidChains(color.opposite());
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
		}
	}

	/**
	 * Removes any chains in the atari lists that are either no longer chains or
	 * no longer in atari.
	 */
	private void removeInvalidChains(StoneColor color) {
		int index = color.index();
		ShortSet chains = chainsInAtari[index];
		for (int i = 0; i < chains.size(); i++) {
			short p = chains.get(i);
			if (board.getColorAt(p) == VACANT || board.getChainRoot(p) != p || board.getLiberties(p).size() > 1) {
				chains.remove(p);
				i--;
			}
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

	@Override
	public void copyDataFrom(BoardObserver that) {
		AtariObserver original = (AtariObserver)that;
		chainsInAtari[BLACK.index()].copyDataFrom(original.chainsInAtari[BLACK.index()]);
		chainsInAtari[WHITE.index()].copyDataFrom(original.chainsInAtari[WHITE.index()]);
	}

}
