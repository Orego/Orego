package edu.lclark.orego.feature;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.NonStoneColor.*;
import edu.lclark.orego.util.ShortSet;

@SuppressWarnings("serial")
public class PatternSuggester implements Suggester {
	
	private static final float THRESHOLD = 0.8f;

	private final Board board;

	private final CoordinateSystem coords;

	private final HistoryObserver history;

	private final ShortSet moves;

	private float[] winRates;

	public PatternSuggester(Board board, HistoryObserver history) {
		this.board = board;
		coords = board.getCoordinateSystem();
		this.history = history;
		moves = new ShortSet(coords.getFirstPointBeyondBoard());
		winRates = new float[0];
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(
					new FileInputStream("PatternData/Pro3x3PatternData.data"));
			int[] fileRuns = (int[]) objectInputStream.readObject();
			int[] fileWins = (int[]) objectInputStream.readObject();
			winRates = new float[fileRuns.length];
			for(int i = 0; i < fileRuns.length; i++){
				winRates[i] = (float)fileWins[i] / (float)fileRuns[i];
			}
			objectInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public ShortSet getMoves() {
		short p = history.get(board.getTurn() - 1);
		short[] neighbors = coords.getNeighbors(p);
		for (short n : neighbors) {
			if (board.getColorAt(n) == VACANT) {
				char hash = calculateHash(n);
				if(winRates[hash] > THRESHOLD){
					moves.add(n);
				}
			}
		}
		return moves;
	}
	
	private char calculateHash(short n){
		char hash = 0;
		short[] neighbors = coords.getNeighbors(n);
		for (int i = 0; i < neighbors.length; i++) {
			Color color = board.getColorAt(neighbors[i]);
			if (color == board.getColorToPlay()) {
				// Friendly stone at this neighbor
				hash |= 1 << (i * 2);
			} else if (color != board.getColorToPlay().opposite()) {
				// neighbor is vacant or off board
				hash |= color.index() << (i * 2);
			} // else do nothing, no need to OR 0 with 0
		}
		return hash;
	}

}
