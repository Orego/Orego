package edu.lclark.orego.book;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;
import static edu.lclark.orego.experiment.Logging.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;

/**
 * Produces moves from a book extracted from strong players' games.
 * 
 * @see FusekiBookBuilder
 */
public final class FusekiBook implements OpeningBook {

	/** The fuseki book proper. */
	private SmallHashMap book;

	/** Don't bother looking in the book after this many moves into the game. */
	private int maxMoves;

	public FusekiBook() {
		this("books");
	}

	/** Gets the hashMap out of the file. */
	@SuppressWarnings("boxing")
	public FusekiBook(String directory) {
		final File file = new File(OREGO_ROOT + directory + File.separator
				+ "fuseki19.data");
		log("Started reading opening book");
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				file))) {
			maxMoves = (Integer) in.readObject();
			book = (SmallHashMap) in.readObject();
			log("Finished reading opening book");
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public short nextMove(Board board) {
		final long fancyHash = board.getFancyHash();
		if (board.getTurn() < maxMoves) {
			if (book.containsKey(fancyHash)) {
				final short move = book.get(fancyHash);
				if (board.isLegal(move)) {
					return move;
				}
			}
		}
		return CoordinateSystem.NO_POINT;
	}
}
