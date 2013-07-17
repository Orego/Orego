package orego.shape;

import static orego.core.Board.*;
import static orego.core.Coordinates.*;
import static orego.shape.PatternPlayer.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Set;

import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.core.Colors;
import orego.sgf.SgfParser;
import orego.util.IntSet;

/**
 * This class looks only at patterns that were actually played. These are sorted
 * based on the ratio of patterns seen to patterns played.
 * 
 * @author galbraith
 */
public class PatternCounterTwoPointOne {

	/** Multihash tables. */
	private static Table[][] tables;

	/** Directory where test files are located. */
	private static String TEST_DIRECTORY = ".." + File.separator + ".."
			+ File.separator + ".." + File.separator + "Desktop"
			+ File.separator + "Test Games" + File.separator;// +"kgs-19-2001"+File.separator;

	public static void main(String[] args) {
		new PatternCounterTwoPointOne();
	}

	public PatternCounterTwoPointOne() {
		try {
			setUp(TEST_DIRECTORY);
			ObjectOutputStream ow = new ObjectOutputStream(
					new FileOutputStream(new File(TEST_DIRECTORY
							+ "patterns.dat")));
			ow.writeObject(tables);
			ow.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Takes a directory of SGF files and walks through them, counting how often
	 * patterns are seen and played.
	 */
	public void setUp(String filepath) {
		try {
			File directory = new File(filepath);
			System.out.println("Directory: " + directory.getAbsolutePath());
			String[] dirList = directory.list();
			for (int i = 0; i < dirList.length; i++) {
				String filename = filepath + File.separator + dirList[i];
				File file = new File(filename);
				if (file.isDirectory()) {
					setUp(filename);
				} else if (dirList[i].toLowerCase().endsWith(".sgf")) {
					checkForPatterns(file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Check for the patterns in a particular file.
	 */
	public void checkForPatterns(File file) {
		Board board = SgfParser.sgfToBoard(file);
		int turn = board.getTurn();
		int currentTurn = 0;
		// TODO What if the game has a handicap?
		Board patternBoard = new Board();
		MersenneTwisterFast random = new MersenneTwisterFast();
		while (currentTurn < turn) {
			int goodMove = board.getMove(currentTurn);
			if (isOnBoard(goodMove)) {
				// Choose a random move to store as bad
				IntSet possibleMoves = patternBoard.getVacantPoints();
				int badMove;
				do {
					badMove = possibleMoves.get(random.nextInt(possibleMoves
							.size()));
				} while (!patternBoard.isLegal(badMove) || badMove == goodMove);
				for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
					tables[radius][patternBoard.getColorToPlay()].store(
							patternBoard.getPatternHash(goodMove, radius), 1);
					tables[radius][patternBoard.getColorToPlay()].store(
							patternBoard.getPatternHash(badMove, radius), 0);
				}
			}
			currentTurn++;
			patternBoard.play(goodMove);
		}
	}

}
