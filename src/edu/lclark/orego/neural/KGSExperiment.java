package edu.lclark.orego.neural;

import static edu.lclark.orego.core.CoordinateSystem.PASS;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import static edu.lclark.orego.core.StoneColor.WHITE;
import static edu.lclark.orego.move.Mover.PRIMES;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.sgf.SgfParser;
import edu.lclark.orego.thirdparty.MersenneTwisterFast;
import edu.lclark.orego.util.ShortSet;

public class KGSExperiment {

	public static void main(String[] args) {
		new KGSExperiment().run();
	}

	//Rounds numbers so they are easier to read in a 19x19 grid
	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private Board board;

	private CoordinateSystem coords;

	private SgfParser parser;

	private final MersenneTwisterFast random;

	public KGSExperiment() {
		board = new Board(19);
		coords = board.getCoordinateSystem();
		parser = new SgfParser(coords, true);
		random = new MersenneTwisterFast();
	}

	//Converts coordinates into a format that works with our array
	private int index(int row, int col) {
		return 19 * row + col;
	}

	private Board obviousTest() {
		Board test = new Board(19);
		String[] blank = { "...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", "...................",
				"...................", };
		test.setUpProblem(blank, WHITE);
		return test;
	}

	private void run() {

		// Get games from file
		final List<List<Short>> games = parser.parseGamesFromFile(new File(
				"sgf-test-files/19/1977-02-27.sgf"), 179);
		int[] day = { 10, 15, 16, 25, 13, 16, 6, 23, 12, 17, 15, 25, 17, 14,
				20, 6, 13, 24, 21, 20, 24, 27, 3, 6, 12, 12, 17, 23, 30, 38, 34 };
		for (int i = 0; i < day.length; i++) {
			for (int j = 0; j < day[i]; j++) {
				if (i < 9) {
					games.addAll(parser.parseGamesFromFile(new File(
							"kgs-19-2015-05-new/2015-05-0" + (i + 1) + "-"
									+ (j + 1) + ".sgf"), 20));
				} else {
					games.addAll(parser.parseGamesFromFile(new File(
							"kgs-19-2015-05-new/2015-05-" + (i + 1) + "-"
									+ (j + 1) + ".sgf"), 20));
				}
			}
		}

		// Count the number of points to train on
		int numberOfTrainingPoints = 0;
		for (final List<Short> game : games) {
			for (final Short move : game) {
				numberOfTrainingPoints++;
			}
		}

		// Declare stuff
		Network net = new Network(19 * 19 * 4, 19 * 19, 1, 19 * 19);
		double[][] training = new double[numberOfTrainingPoints][19 * 19 * 4];
		double[][] trainingCorrect = new double[numberOfTrainingPoints][4];
		int gameNumber = 0;

		// Input data
		for (final List<Short> game : games) {
			int k = 0;
			for (final Short move : game) {
				board.play(move);
				int p = 0; // place in training array
				Extractor extractor = new Extractor(board);
				for (int row = 0; row < 19; row++) {
					for (int col = 0; col < 19; col++) {
						training[k][p] = extractor.isBlack(row, col);
						training[k][p + 19 * 19] = extractor.isWhite(row, col);
						training[k][p + 19 * 19 * 2] = extractor
								.isUltimateMove(row, col);
						training[k][p + 19 * 19 * 3] = extractor
								.isPenultimateMove(row, col);
						p++;
					}
				}
				short rand = selectRandomMove(move);
				trainingCorrect[k] = new double[] {
						index(coords.row(move), coords.column(move)),
						index(coords.row(rand), coords.column(rand)) };
				System.out.println("Game " + (gameNumber + 1) + ", Move "
						+ (k + 1) + ": " + trainingCorrect[k][0] + ", "
						+ trainingCorrect[k][1]);
				if (trainingCorrect[k][1] < 0) {
					break;
				}
				net.train(1, (int) trainingCorrect[k][0], training[k]);
				net.train(0, (int) trainingCorrect[k][1], training[k]);
				k++;
			}
			gameNumber++;
		}

		// Train the network
		for (int i = 0; i < 1000; i++) {
			int k = (int) (numberOfTrainingPoints * Math.random());
			net.train(1, (int) trainingCorrect[k][0], training[k]);
			net.train(0, (int) trainingCorrect[k][1], training[k]);
		}

		// Make test data
		board = obviousTest();
		double[] testObvious = new double[19 * 19 * 4];
		Extractor extractor = new Extractor(board);
		int p = 0;
		for (int row = 0; row < 19; row++) {
			for (int col = 0; col < 19; col++) {
				testObvious[p] = extractor.isBlack(row, col);
				testObvious[p + 19 * 19] = extractor.isWhite(row, col);
				testObvious[p + 19 * 19 * 2] = extractor.isUltimateMove(row,
						col);
				testObvious[p + 19 * 19 * 3] = extractor.isPenultimateMove(row,
						col);
				p++;
			}
		}
		net.test(testObvious);//possibly delete this line
		
		//Print test data
		for (int j = 0; j < 19 * 19; j++) {
			System.out.print(round(net.test(testObvious)[j], 5) + "\t");
			if (j % 19 == 18) {
				System.out.println();
			}
		}
		System.out.println();

	}

	//Copy and pasted from a different part of Orego that selects 
	//a different move other then the one inputed
	short selectRandomMove(short move) {
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
}
