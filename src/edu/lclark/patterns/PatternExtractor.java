package edu.lclark.patterns;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import ec.util.MersenneTwisterFast;
import edu.lclark.orego.core.*;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.move.Mover.*;

public final class PatternExtractor {

	private final static int PATTERN_COUNT = 65536;

	private final Board board;

	private final CoordinateSystem coords;

	private final int[] runs;

	private final int[] wins;

	private final MersenneTwisterFast random;

	private final ArrayList<Pattern> list;

	private final SgfParser parser;

	private static final String OUTPUT_FILE = "PatternData/Pro3x3PatternData.txt";

	/**
	 * Analyzes 3x3 patterns in SGF files and stores a win rate for each in an
	 * array. Does not support varying board sizes because SgfParser does not
	 * currently support board sizes other than 19.
	 */
	public PatternExtractor() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		runs = new int[PATTERN_COUNT];
		wins = new int[PATTERN_COUNT];
		random = new MersenneTwisterFast();
		list = new ArrayList<>();
		parser = new SgfParser(coords);
	}

	/**
	 * Updates the tables with a win for the pattern around the move and a loss
	 * for some other random move on the board.
	 */
	void analyzeMove(short move) {
		updateTables(true, move);
		updateTables(false, selectRandomMove(move));
		board.play(move);
	}

	/**
	 * Updates tables by generating hashes for patterns based on the color to
	 * play. Color reversed patterns are stored in the same slot. If the move is
	 * considered good, winner is true and the wins for the slot are updated. If
	 * not, only runs is updated.
	 */
	private void updateTables(boolean winner, short move) {
		short[] neighbors = coords.getNeighbors(move);
		int[] hashPieces = new int[8];
		int hash = 0;
		for (int i = 0; i < neighbors.length; i++) {
			Color color = board.getColorAt(neighbors[i]);
			if (color == board.getColorToPlay()) {
				// Friendly stone at this neighbor
				hashPieces[i] = 1;
			} else if (color != board.getColorToPlay().opposite()) {
				// neighbor is vacant or off board
				hashPieces[i] = color.index();
			} else {
				hashPieces[i] = 0;
			}
		}
		updateOriginalPattern(winner, hashPieces);
		updateRotations(winner, hashPieces);
		updateReflections(winner, hashPieces);
	}

	private void updateOriginalPattern(boolean winner, int[] hashPieces) {
		int hash = 0;
		for(int i = 0; i < 8; i++){
			hash |= hashPieces[i] << (i * 2);
		}
		if (winner) {
			wins[hash] += 1;
		}
		runs[hash] += 1;
	}

	private void updateReflections(boolean winner, int[] hashPieces) {
		int[] tempPieces = hashPieces;
		tempPieces = reflectOverDiagonal(hashPieces);
		for(int i = 0; i < 4; i++){
			tempPieces = rotate90Degrees(tempPieces);
			int hash = 0;
			for(int j = 0; j < 8; j++){
				hash |= tempPieces[j] << (j * 2);
			}
			System.out.println(hash);
			if (winner) {
				wins[hash] += 1;
			}
			runs[hash] += 1;
		}
	}

	private void updateRotations(boolean winner, int[] hashPieces) {
		int[] tempPieces = hashPieces;
		for(int i = 0; i < 3; i++){
			tempPieces = rotate90Degrees(tempPieces);
			int hash = 0;
			for(int j = 0; j < 8; j++){
				hash |= hashPieces[j] << (i * 2);
			}
			if (winner) {
				wins[hash] += 1;
			}
			runs[hash] += 1;
		}
	}

	private int[] rotate90Degrees(int[] hashPieces) {
		int[] tempPieces = new int[8];
		tempPieces[0] = hashPieces[1];
		tempPieces[1] = hashPieces[3];
		tempPieces[2] = hashPieces[0];
		tempPieces[3] = hashPieces[2];
		tempPieces[4] = hashPieces[6];
		tempPieces[5] = hashPieces[4];
		tempPieces[6] = hashPieces[7];
		tempPieces[7] = hashPieces[5];
		return tempPieces;
		
	}
	
	private int[] reflectOverDiagonal(int[] hashPieces){
		int[] tempPieces = new int[8];
		tempPieces[0] = hashPieces[2];
		tempPieces[1] = hashPieces[3];
		tempPieces[2] = hashPieces[0];
		tempPieces[3] = hashPieces[1];
		tempPieces[4] = hashPieces[7];
		tempPieces[5] = hashPieces[5];
		tempPieces[6] = hashPieces[6];
		tempPieces[7] = hashPieces[4];
		return tempPieces;
	}

	/**
	 * Selects a random move. Used to help balance the table with losses for
	 * points not played in the game being analyzed.
	 */
	private short selectRandomMove(short move) {
		ShortSet vacantPoints = board.getVacantPoints();
		short start = (short) (random.nextInt(vacantPoints.size()));
		short i = start;
		short skip = PRIMES[random.nextInt(PRIMES.length)];
		do {
			short p = vacantPoints.get(i);
			if ((board.getColorAt(p) == VACANT)) {
				if (board.isLegal(p) && p != move) {
					return p;
				}
			}
			// Advancing by a random prime skips through the array
			// in a manner analogous to double hashing.
			i = (short) ((i + skip) % vacantPoints.size());
		} while (i != start);
		return PASS;
	}

	/** Analyzes all the games in one SGF file. */
	private void analyzeGames(List<List<Short>> games) {
		for (List<Short> game : games) {
			for (Short move : game) {
				analyzeMove(move);
			}
			board.clear();
		}

	}
	
	float getWinRate(int hash){
		if(runs[hash] == 0){
			return 0.5f;
		}
		return (float)wins[hash] / (float)runs[hash];
	}

	private List<Pattern> getPatterns() {
		return list;
	}

	private void buildPatternData(File file) {
		analyzeFiles(file);
		FileOutputStream out;
		try {
			out = new FileOutputStream("PatternData/Pro3x3PatternData.data");
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(runs);
			oos.writeObject(wins);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	private void analyzeFiles(File file) {
		File[] allFiles = file.listFiles();
		if (allFiles != null) {
			for (File tempFile : allFiles) {
				analyzeFiles(tempFile);
			}
		} else {
			if (file.getPath().endsWith(".sgf")) {
				List<List<Short>> games = parser.parseGamesFromFile(file, 500);
				analyzeGames(games);
			}
		}
	}

	public static void main(String[] args) {
		PatternExtractor extractor = new PatternExtractor();
		int highestRuns = 0;
		extractor
				.buildPatternData(new File(
						"/Network/Servers/maccsserver.lclark.edu/Users/slevenick/Desktop/patternfiles"));
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(
					new FileInputStream("PatternData/Pro3x3PatternData.data"));
			int[] fileRuns = (int[]) objectInputStream.readObject();
			int[] fileWins = (int[]) objectInputStream.readObject();
			objectInputStream.close();

			for (int i = 0; i < PATTERN_COUNT; i++) {
				if (fileRuns[i] != 0) {
					extractor.list.add(new Pattern(i, (float) fileWins[i]
							/ (float) fileRuns[i], fileRuns[i]));
					if (fileRuns[i] > highestRuns) {
						if (i != 43690) {
							highestRuns = fileRuns[i];
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		Collections.sort(extractor.list);
		try (PrintWriter writer = new PrintWriter(OUTPUT_FILE, "UTF-8")) {
			for (Pattern pattern : extractor.getPatterns()) {
				writer.println(pattern.getWinRate() + ","
						+ (float) pattern.getRuns() / highestRuns);
				if (pattern.getWinRate() > .79 && pattern.getWinRate() < .81) {
					System.out.println(pattern);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	class ValueComparator implements Comparator<Integer> {
		private Map<Integer, Float> map;

		ValueComparator(HashMap<Integer, Float> map) {
			this.map = map;
		}

		@Override
		public int compare(Integer key1, Integer key2) {
			if (map.get(key1) > map.get(key2)) {
				return 1;
			} else {
				return -1;
			}
		}

	}
}
