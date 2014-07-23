package edu.lclark.orego.patterns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Legality;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

public class ShapeExtractor extends PatternExtractor {

	private ShapeTable shapeTable;

	private float scalingFactor;

	private int patternSize;

	private Board[] boards;

	public static void main(String[] args) {
		ShapeExtractor extractor = new ShapeExtractor(true, 0.99f, 80);
		extractor.buildPatternData(new File(
				"/Network/Servers/maccsserver.lclark.edu/Users/mdreyer/Desktop/KGS Files"));
	}

	public ShapeExtractor(boolean verbose, float scalingFactor, int patternSize) {
		super(verbose);
		this.patternSize = patternSize;
		this.scalingFactor = scalingFactor;
		shapeTable = new ShapeTable(scalingFactor);
		boards = new Board[8];
		for (int i = 0; i < 8; i++) {
			boards[i] = new Board(19);
		}
	}
	
	void analyzeMove(short move, Board board) {
		updateTables(true, move, board);
		updateTables(false, selectRandomMove(move), board);
	}
	
	@Override
	void buildPatternData(File inputFile){
		int patternDiameter = (int) Math.sqrt(patternSize + 1);
		buildPatternData(inputFile.getPath(), OREGO_ROOT + "patterns/patterns"
				+ patternDiameter + "x" + patternDiameter + "-SHAPE-sf"
				+ (int) (scalingFactor * 100) + ".data");
	}

	void buildPatternData(String inputFile, String outputFile) {
		analyzeFiles(new File(inputFile));
		try (FileOutputStream out = new FileOutputStream(outputFile);
				ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(shapeTable.getWinRateTables());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/** Returns the point at move reflected over the line c = r. */
	public short reflect(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = coords.getWidth() - 1 - col;
		final int c2 = coords.getWidth() - 1 - row;
		final short p = coords.at(r2, c2);
		return p;
	}

	/** Returns the point at move rotated counterclockwise by 90 degrees. */
	public short rotate90(short move) {
		final int row = coords.row(move);
		final int col = coords.column(move);
		final int r2 = coords.getWidth() - 1 - col;
		final int c2 = row;
		final short p = coords.at(r2, c2);
		return p;
	}

	@SuppressWarnings("boxing")
	@Override
	void analyzeGames(List<List<Short>> games) {
		for (List<Short> game : games) {
			for (Board board : boards) {
				board.clear();
			}
			for (Short move : game) {
				final short[] transformations = new short[8];
				transformations[0] = move;
				transformations[1] = rotate90(move);
				transformations[2] = rotate90(transformations[1]);
				transformations[3] = rotate90(transformations[2]);
				transformations[4] = reflect(move);
				transformations[5] = rotate90(transformations[4]);
				transformations[6] = rotate90(transformations[5]);
				transformations[7] = rotate90(transformations[6]);
				Legality legality = Legality.OK;
				for (int i = 0; i < transformations.length; i++) {
					analyzeMove(transformations[i], boards[i]);
					legality = boards[i].play(transformations[i]);
					if (legality == Legality.KO_VIOLATION) {
						break;
					} else if (legality == Legality.SUICIDE) {
						throw new IllegalArgumentException(
								"SGF contained illegal move at "
										+ coords.toString(transformations[i]) + " on turn "
										+ boards[i].getTurn() + "\n" + boards[i]);
					}
				}
				if(legality == Legality.KO_VIOLATION){
					break;
				}
			}
		}
	}

	void updateTables(boolean winner, short move, Board board) {
		long hash = PatternFinder.getHash(board, move, patternSize);
		shapeTable.update(hash, winner);
	}

}
