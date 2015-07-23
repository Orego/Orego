package edu.lclark.orego.patterns;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.lclark.orego.core.*;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortList;
import static edu.lclark.orego.core.CoordinateSystem.*;
import static edu.lclark.orego.core.NonStoneColor.*;

/** Extracts patterns from a directory of recorded SGF games. */
public class PatternExtractor {

	/** Number of possible patterns. */
	private final static int PATTERN_COUNT = Character.MAX_VALUE + 1;

	private final ShortList candidates;

	public static void main(String[] args) {
//		 Uncomment the code below to rebuild the pattern database
		 PatternExtractor extractor = new PatternExtractor(true);
		 extractor
		 .buildPatternData(new File(
		 "/Network/Servers/maccsserver.lclark.edu/Users/slevenick/Desktop/patternfiles"));
		// Uncomment the line below to print the patterns
//		printPatterns(0.98, 1.00);
	}

	/**
	 * Prints, in human-readable form, all patterns with win rates at least lo
	 * and at most hi. # represents an enemy stone, O friendly, ? off-board.
	 */
	static void printPatterns(double lo, double hi) {
		int highestRuns = 0;
		List<Pattern> list = new ArrayList<>();
		try (ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream("patterns/patterns3x3.data"))) {
			int[] fileRuns = (int[]) objectInputStream.readObject();
			int[] fileWins = (int[]) objectInputStream.readObject();
			for (int i = 0; i < PATTERN_COUNT; i++) {
				if (fileRuns[i] != 0) {
					list.add(new Pattern(i, (float) fileWins[i]
							/ (float) fileRuns[i], fileRuns[i]));
					if (fileRuns[i] > highestRuns) {
						// Exclude the all-vacant pattern
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
		Collections.sort(list);
		for (Pattern pattern : list) {
			if (pattern.getWinRate() >= lo && pattern.getWinRate() <= hi) {
				System.out.println(pattern);
			}
		}
	}

	/** Returns a version of the pattern colors, reflected across the diagonal. */
	private static int[] reflectAcrossDiagonal(int[] colors) {
		int[] result = new int[8];
		result[0] = colors[2];
		result[1] = colors[3];
		result[2] = colors[0];
		result[3] = colors[1];
		result[4] = colors[7];
		result[5] = colors[5];
		result[6] = colors[6];
		result[7] = colors[4];
		return result;
	}

	/** Returns a version of the pattern colors, rotated 90 degrees clockwise. */
	private static int[] rotate90Degrees(int[] colors) {
		int[] result = new int[8];
		result[0] = colors[1];
		result[1] = colors[3];
		result[2] = colors[0];
		result[3] = colors[2];
		result[4] = colors[6];
		result[5] = colors[4];
		result[6] = colors[7];
		result[7] = colors[5];
		return result;
	}

	/** Used to play moves. */
	private final Board board;

	final CoordinateSystem coords;

	private final SgfParser parser;

	private final MersenneTwisterFast random;

	/** runs[i] is the number of times move i was selected. */
	private final int[] runs;

	/**
	 * If true, prints out messages indicating progress when processing a
	 * directory.
	 */
	private final boolean verbose;

	/**
	 * wins[i] * runs[i] is the number of times move i was played in a recorded
	 * game.
	 */
	private final int[] wins;

	/**
	 * Analyzes 3x3 patterns in SGF files and stores a win rate for each in an
	 * array. Does not support varying board sizes because SgfParser does not
	 * currently support board sizes other than 19.
	 */
	public PatternExtractor(boolean verbose) {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		runs = new int[PATTERN_COUNT];
		wins = new int[PATTERN_COUNT];
		random = new MersenneTwisterFast();
		parser = new SgfParser(coords, true);
		this.verbose = verbose;
		candidates = new ShortList(coords.getArea());
	}

	/**
	 * Processes file, updating counts of patterns encountered. If file is a
	 * folder, recursively descends into it.
	 */
	void analyzeFiles(File file) {
		File[] allFiles = file.listFiles();
		if (file.isDirectory()) {
			if (verbose) {
				System.out.println("Analyzing files in " + file.getName());
			}
			for (File tempFile : allFiles) {
				analyzeFiles(tempFile);
			}
		} else {
			if (file.getPath().endsWith(".sgf")) {
				List<List<Short>> games = parser.parseGamesFromFile(file,
						Integer.MAX_VALUE);
				try {
					analyzeGames(games);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					System.err.println(file.getPath());
					System.exit(1);
				}
			}
		}
	}

	/** Analyzes all the games in one SGF file. */
	@SuppressWarnings("boxing")
	void analyzeGames(List<List<Short>> games) {
		for (List<Short> game : games) {
			for (Short move : game) {
				analyzeMove(move);
				Legality legality = board.play(move);
				if (legality == Legality.KO_VIOLATION) {
					break;
				} else if (legality == Legality.SUICIDE) {
					throw new IllegalArgumentException(
							"SGF contained illegal move at "
									+ coords.toString(move) + " on turn "
									+ board.getTurn() + "\n" + board);
				}
			}
			board.clear();
		}
	}

	/**
	 * Updates the tables with a win for the pattern around the move and a loss
	 * for some other random move on the board.
	 */
	void analyzeMove(short move) {
		updateTables(true, move);
		updateTables(false, selectRandomMove(move));
	}

	/**
	 * Creates the pattern database based on SGF files
	 * 
	 * @param file
	 *            Either one SGF file or a possibly nested directory containing
	 *            SGF files.
	 */
	void buildPatternData(File file) {
		analyzeFiles(file);
		try (FileOutputStream out = new FileOutputStream(
				"patterns/patterns3x3.data");
				ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(runs);
			oos.writeObject(wins);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	Board getBoard() {
		return board;
	}

	/** For testing. */
	float getWinRate(int colors) {
		if (runs[colors] == 0) {
			return 0.5f;
		}
		return (float) wins[colors] / (float) runs[colors];
	}

	/**
	 * Selects a random legal move. Used to help balance the table with losses
	 * for points not played in the game being analyzed.
	 */
	short selectRandomMove(short move) {
		candidates.clear();
		candidates.addAll(board.getVacantPoints());
		while (candidates.size() > 0) {
			final short p = candidates.removeRandom(random);
			if ((board.getColorAt(p) == VACANT)) {
				if (board.isLegal(p) && p != move) {
					return p;
				}
			}
		} 
		return PASS;
	}

	/**
	 * Updates all four rotations of colors.
	 * 
	 * @param winner
	 *            True if this was a winning move.
	 * @param colors
	 *            Colors of the neighbors, in the order specified in
	 *            CoordinateSystem.
	 * 
	 * @see edu.lclark.orego.core.CoordinateSystem
	 */
	private void updateRotations(boolean winner, int[] colors) {
		for (int i = 0; i < 4; i++) {
			int hash = 0;
			for (int j = 0; j < 8; j++) {
				hash |= colors[j] << (j * 2);
			}
			if (winner) {
				wins[hash] += 1;
			}
			runs[hash] += 1;
			colors = rotate90Degrees(colors);
		}
	}

	/**
	 * Updates win and run stats for the pattern around move. If the move is
	 * considered good, winner is true and the wins for the slot are updated. If
	 * not, only runs is updated.
	 */
	void updateTables(boolean winner, short move) {
		short[] neighbors = coords.getNeighbors(move);
		int[] colors = new int[8];
		for (int i = 0; i < neighbors.length; i++) {
			// Color of neighbor
			Color color = board.getColorAt(neighbors[i]);
			if (color == board.getColorToPlay()) {
				// Friendly stone
				colors[i] = 1;
			} else if (color == board.getColorToPlay().opposite()) {
				// Enemy stone
				colors[i] = 0;
			} else {
				// Vacant or off board
				colors[i] = color.index();
			}
		}
		updateRotations(winner, colors);
		updateRotations(winner, reflectAcrossDiagonal(colors));
	}

}
