package orego.shape;

import static orego.experiment.ExperimentConfiguration.SGF_DIRECTORY;
import static orego.core.Coordinates.*;
import static orego.core.Colors.*;
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
public class MultiStageFilterExtractor {

	private static String outputDirectory = "SgfFiles";
	
	/** Multihash tables, indexed by radius and color to play. */
	private MultiStageFilter multiFilter;
	
	private MersenneTwisterFast random;

	private static int[][] parameters = {
//		{1,18},{2,17},
		{4,16}
//	,
//		{8,8}
//		,
//		{16,4}

	};
	
	public static void main(String[] args) {
		//System.out.println(new File(inputDirectory).getAbsolutePath());
		for (int i = 0; i < parameters.length; i++) {
			new MultiStageFilterExtractor().run(SGF_DIRECTORY, outputDirectory, parameters[i][0], parameters[i][1]);
		}
	}
	
/**
 * Extracts patterns from all files in a directory.
 * 
 * @param in Full path to directory containing SGF files.
 * @param out Directory (within OREGO_ROOT_DIRECTORY) to store output, usually "SgfFiles" or "SgfTestFiles".
 */
	public void run(String in, String out, int t, int b) {
		multiFilter = new MultiStageFilter(t,b);
		try {
			random = new MersenneTwisterFast(0L);
			setUp(in);
			ObjectOutputStream ow = new ObjectOutputStream(
					new FileOutputStream(new File(orego.experiment.Debug.OREGO_ROOT_DIRECTORY + out + File.separator + "Filter-r"+Board.MAX_PATTERN_RADIUS+"-t"+t+"-b"+b+".data")));
			ow.writeObject(multiFilter);
			ow.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/** Returns the cluster created by this extractor. */
	protected MultiStageFilter getMultiStageFilter() {
		return multiFilter;
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
			if (dirList != null) {
				for (int i = 0; i < dirList.length; i++) {
					String filename = directory + File.separator + dirList[i];
					File file = new File(filename);
					if (file.isDirectory()) {
						setUp(filename);
					} else if (dirList[i].toLowerCase().endsWith(".sgf")) {
						checkForPatterns(file);
					}
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
		if (board.getInitialBlackStones().size()!=0||board.getInitialWhiteStones().size()!=0){
			//handicap game, so ignore it
			return;
		}
		Board[][][] patternBoard = new Board[4][2][2];
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int reflection = 0; reflection < 2; reflection++) {
				for (int color = 0; color < 2; color++) {
					patternBoard[rotation][reflection][color] = new Board();					
				}
				patternBoard[rotation][reflection][WHITE].setColorToPlay(WHITE);					
			}
		}
		int turn = board.getTurn();
		int currentTurn = 0;
		while (currentTurn < turn) {
			int goodMove = board.getMove(currentTurn);
			if (isOnBoard(goodMove)) {
				// Store in all 16 rotations, reflections, and color inversions
				for (int rotation = 0; rotation < 4; rotation++) {
					for (int reflection = 0; reflection < 2; reflection++) {
						for (int color = 0; color < 2; color++) {
							multiFilter.store(patternBoard[rotation][reflection][color], goodMove);
							//play the move
							patternBoard[rotation][reflection][color].play(goodMove);
						}
						goodMove = reflect(goodMove);
					}
					goodMove = rotate(goodMove);
				}
			}
			else{
				//play the move
				for (int rotation = 0; rotation < 4; rotation++) {
					for (int reflection = 0; reflection < 2; reflection++) {
						for (int color = 0; color < 2; color++) {
							patternBoard[rotation][reflection][color].play(goodMove);
						}
					}
				}
			}
			currentTurn++;
		}
	}
}
