package edu.lclark.orego.patterns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.core.Legality;
import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

public class ShapeExtractor extends PatternExtractor {

	private ShapeTable shapeTable;

	private float scalingFactor;

	private int minStones;

	private Board[] boards;

	public static void main(String[] args) {
		for (int stones = 3; stones <= 9; stones++) {
			ShapeExtractor extractor = new ShapeExtractor(true, 0.999f, stones);
			extractor.buildPatternData(new File(
					"/Users/drake/Documents/kgs-expert-games"));
		}
	}

	public ShapeExtractor(boolean verbose, float scalingFactor, int minStones) {
		super(verbose);
		this.minStones = minStones;
		this.scalingFactor = scalingFactor;
		shapeTable = new ShapeTable(scalingFactor);
		boards = new Board[8];
		for (int i = 0; i < 8; i++) {
			boards[i] = new Board(19);
		}
	}
	
	void analyzeMove(short move, Board board, short lastMove) {
		updateTables(true, move, board, lastMove);
		updateTables(false, selectRandomMove(move), board, lastMove);
	}
	
	@Override
	void buildPatternData(File inputFile){
		String sfString = Float.toString(scalingFactor);
		sfString = sfString.substring(sfString.indexOf('.') + 1);
		buildPatternData(inputFile.getPath(), OREGO_ROOT + "patterns/patterns"
				+ minStones + "stones-SHAPE-sf"
				+ sfString + ".data");
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
			short[] lastMoves = new short[8];
			Arrays.fill(lastMoves, CoordinateSystem.NO_POINT);
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
					analyzeMove(transformations[i], boards[i], lastMoves[i]);
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
				for(int i = 0; i<transformations.length; i++){
					lastMoves[i] = transformations[i];
				}
			}
		}
	}

	void updateTables(boolean winner, short move, Board board, short lastMove) {
		long hash = PatternFinder.getHash(board, move, minStones, lastMove);
		shapeTable.update(hash, winner);
	}

}
