package orego.patternanalyze;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import orego.core.Board;
import orego.core.SgfParser;
import orego.patterns.RatedPattern;
import orego.util.BitVector;
import static orego.patterns.Pattern.*;
import static orego.core.Board.*;

public class PatternCounter5 {

	/**
	 * The number of total patterns, including impossible ones.
	 */
	public static final int NUMBER_OF_NEIGHBORHOODS = Character.MAX_VALUE + 1;

	/** Seen[p] is the number of times p was seen. */
	protected int[] seen;
	
	/** Played[p] is the number of times p was played. */
	protected int[] played;
	
	public static void main(String[] args) {
		new PatternCounter5().run();
	}
	
	protected BitVector seenThisGame;
	
	protected BitVector playedThisGame;

	protected String outputFile;
	
	protected int numberOfGames = 0;
	
	public PatternCounter5() {
		playedThisGame = new BitVector(NUMBER_OF_NEIGHBORHOODS);
		seenThisGame = new BitVector(NUMBER_OF_NEIGHBORHOODS);	
		outputFile = "GoodPatterns2";
	}
	
	private static String TEST_DIRECTORY = "../../../Test Games/";

	public void run() {
		played = new int[NUMBER_OF_NEIGHBORHOODS];
		seen = new int[NUMBER_OF_NEIGHBORHOODS];
		readGames(TEST_DIRECTORY);
		writeOutput();
	}

	public void writeOutput()  {
		try {
			TreeSet<RatedPattern> patterns = new TreeSet<RatedPattern>();
			for (int p = 0; p < NUMBER_OF_NEIGHBORHOODS; p++) {
				if (seen[p] > 0) {
					patterns.add(new RatedPattern((char)p, (double)played[p] / seen[p]));
				}
			}
			PrintWriter bw = new PrintWriter(new FileWriter(new File(TEST_DIRECTORY + outputFile + ".txt")));
			for (RatedPattern p : patterns) {
				char pattern = p.getPattern();
				bw.println("\"" + arrayToString(neighborhoodToArray(pattern)) + "\", // "
						+ played[pattern] + "/" + seen[pattern] + " = " + p.getRatio());
			}
			System.out.println("Done.");
			System.out.println("Written to file "+TEST_DIRECTORY + outputFile + ".txt");
			bw.close();
			System.out.println("Number of games:" + numberOfGames);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void readGames(String path) {
		try {
			File directory = new File(path);
			System.out.println("Directory: " + directory.getAbsolutePath());
			String[] dirList = directory.list();
			for (int i = 0; i < dirList.length; i++) {
				String filename = path + File.separator + dirList[i];
				File file = new File(filename);
				if (file.isDirectory()) {
					readGames(filename);
				} else if (dirList[i].toLowerCase().endsWith(".sgf")) {
					Board board = sgfToGame(file);
					if (board != null) {						
						analyze(board);
						numberOfGames++;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public Board sgfToGame(File file) {
		System.out.println("Starting file "+file.getName());
		Board board = SgfParser.sgfToBoard(file);
		if((board.getInitialBlackStones().size() != 0) 
				|| (board.getInitialWhiteStones().size() != 0)) {
			// Handicap Game; ignore it.
			return null;
		}
		System.out.println("Finished file "+file.getName());
		return board;
	}
	
	protected int[] getPointsToAnalyze(int p) {
		return getNeighbors(p);
	}

	public void analyze(Board board) {
		int turn = board.getTurn();
		Board patternBoard = new Board();
		playedThisGame.clear();
		seenThisGame.clear();
		int legality = patternBoard.play(board.getMove(0));
		assert legality == PLAY_OK;
		for (int t = 1; t < turn; t++) {
			int currentPlay = board.getMove(t);
			int lastPlay = board.getMove(t - 1);
			if (isOnBoard(lastPlay) && isOnBoard(currentPlay)) {
				for (int p : getPointsToAnalyze(lastPlay)) {
					if (patternBoard.getColor(p) == VACANT) {
						char neighborhood;
						if (patternBoard.getColorToPlay() == BLACK) {
							neighborhood = patternBoard.getNeighborhood(p);
						} else {
							neighborhood = patternBoard.getNeighborhoodColorsReversed(p);
						}
						char transformed = getLowestTransformation(neighborhood);
						seen[transformed]++;
						assert seen[transformed] >= 0;	
						if (p == currentPlay) {
							played[transformed]++;
							assert played[transformed] >= 0;
						}
					}
				}
			}
			assert patternBoard.isLegal(currentPlay);
			legality = patternBoard.play(currentPlay);
			assert legality == PLAY_OK : "Illegal move #" + t + " at " + pointToString(currentPlay) + " on board:\n" + patternBoard;
		}
	}

}
