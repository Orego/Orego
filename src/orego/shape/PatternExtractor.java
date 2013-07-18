package orego.shape;

import static orego.core.Board.*;
import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import ec.util.MersenneTwisterFast;
import orego.core.Board;
import orego.sgf.SgfParser;
import orego.util.IntSet;

/**
 * Extracts patterns from SGF files.
 */
public class PatternExtractor {

	/** Multihash tables, indexed by radius and color to play. */
	private static Table[][] tables;

	public static void main(String[] args) {
		new PatternExtractor().run("SgfFiles");
	}

	public PatternExtractor() {
		tables = new Table[MAX_PATTERN_RADIUS + 1][NUMBER_OF_PLAYER_COLORS];
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius++) {
			for (int color = BLACK; color <= WHITE; color++) {
				tables[radius][color] = new Table(4, 16);
			}
		}
	}
	
	/** Extracts patterns from all files in directory, which is usually "SgfFiles" or "SgfTestFiles". */
	public void run(String directory) {
		String dir = orego.experiment.Debug.OREGO_ROOT_DIRECTORY + directory
				+ File.separator + getBoardWidth();
		try {
			setUp(dir);
			ObjectOutputStream ow = new ObjectOutputStream(
					new FileOutputStream(new File(dir + File.separator + "patterns.dat")));
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
	public void setUp(String directory) {
		try {
			File dir = new File(directory);
			System.out.println("Directory: " + dir.getAbsolutePath());
			String[] dirList = dir.list();
			for (int i = 0; i < dirList.length; i++) {
				String filename = directory + File.separator + dirList[i];
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
