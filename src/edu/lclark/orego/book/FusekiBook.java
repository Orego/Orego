package edu.lclark.orego.book;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

public class FusekiBook {

	/** The fuseki book proper. */
	private Map<Long, Short> book;
	
	private int maxMoves;

	/** Gets the hashMap out of the file. */
	@SuppressWarnings({ "unchecked", "boxing" })
	public FusekiBook(String inputFileName) {
		File file = new File(OREGO_ROOT + inputFileName + File.separator + "FusekiBook"
				+ "19" + ".data");
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(
				file))) {
			maxMoves = (Integer) (in.readObject());
			book = (HashMap<Long, Short>) (in.readObject());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@SuppressWarnings("boxing")
	public short nextMove(Board board) {
		long boardHash = board.getFancyHash();
		if (board.getTurn() < maxMoves) {
			if (book.containsKey(boardHash)) {
				short move = book.get(boardHash);
				if (board.isLegal(move)) {
					return move;
				}
			}
		}
		return CoordinateSystem.NO_POINT;
	}
}
