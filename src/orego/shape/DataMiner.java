package orego.shape;

import static orego.core.Colors.BLACK;
import static orego.core.Colors.NUMBER_OF_PLAYER_COLORS;
import static orego.core.Colors.VACANT;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.charToColor;
import static orego.core.Coordinates.getBoardWidth;
import static orego.core.Coordinates.isOnBoard;
import static orego.core.Coordinates.reflect;
import static orego.core.Coordinates.rotate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import orego.core.Board;
import orego.sgf.SgfParser;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

/**
 * Extracts patterns from SGF files.
 */
public class DataMiner {

	/** Hash table containing the actual win rates for each pattern. */
	private HashMap<String, Float>[][] winRateMap;
	
	/** Hash table containing the actual count for each pattern. */
	private HashMap<String, Long>[][] countMap;
	
	private Cluster patterns;
	
	private MersenneTwisterFast random;
	
	private static final int MAX_PATTERN_RADIUS = 1;

	public static void main(String[] args) {
		new DataMiner().run(PatternExtractor.TEST_GAMES_DIRECTORY,"SgfFiles");
	}

	@SuppressWarnings("unchecked")
	public DataMiner() {
		winRateMap = new HashMap[MAX_PATTERN_RADIUS+1][NUMBER_OF_PLAYER_COLORS];
		countMap = new HashMap[MAX_PATTERN_RADIUS+1][NUMBER_OF_PLAYER_COLORS];
		for (int radius = 1; radius <= MAX_PATTERN_RADIUS; radius ++){
			for (int color = 0; color < NUMBER_OF_PLAYER_COLORS; color++){
				winRateMap[radius][color]= new HashMap<String,Float>();
				countMap[radius][color]= new HashMap<String,Long>();
			}
		}
	}

	/**
	 * Extracts patterns from all files in a directory.
	 * 
	 * @param in Full path to directory containing SGF files.
	 * @param out Directory (within OREGO_ROOT_DIRECTORY) to store output and load Cluster from, usually "SgfFiles" or "SgfTestFiles".
	 */
	public void run(String in, String out) {
		out = orego.experiment.Debug.OREGO_ROOT_DIRECTORY + out
				+ File.separator + getBoardWidth() + File.separator;
		try {
			random = new MersenneTwisterFast(0L);
			setUp(in);
			
			try {
				ObjectOutputStream ow = new ObjectOutputStream(
						new FileOutputStream(new File(out + "actual-win-rate.data")));
				ow.writeObject(winRateMap);
				ow.flush();
				ow.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			try {
				ObjectOutputStream ow = new ObjectOutputStream(
						new FileOutputStream(new File(out + "actual-count.data")));
				ow.writeObject(countMap);
				ow.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			loadCluster(out);
			
			for(int radius = 1; radius<=MAX_PATTERN_RADIUS; radius++){
				PrintWriter bw = new PrintWriter(new FileWriter(new File(
						out+"results_for_radius"+radius+".txt")));
				StringBuilder output = new StringBuilder("");
				for(String key : winRateMap[radius][BLACK].keySet()){
					output.append(key);
					output.append(',');
					output.append(keyToHash(key,radius));
					output.append(',');
					output.append(winRateMap[radius][BLACK].get(key));
					output.append(',');
					output.append(countMap[radius][BLACK].get(key));
					output.append(',');
					output.append(patterns.getPatternWinRate(keyToHash(key,radius), BLACK, radius));
					output.append(',');
					output.append(patterns.getPatternCount(keyToHash(key,radius), BLACK, radius));
					output.append("\n");
				}
				bw.println(output.toString());
				bw.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	protected static long keyToHash(String key, int radius) {
		long hash = 0L;
		for (int i=0; i<key.length(); i++){
			if (charToColor(key.charAt(i))!=VACANT)
				hash ^= Board.PATTERN_ZOBRIST_HASHES[radius][charToColor(key.charAt(i))][i];
		}
		return hash;
	}

	/**
	 * Takes a directory of SGF files and walks through them, counting how often
	 * patterns are seen and played.
	 */
	public void setUp(String directory) {
		try {
			File dir = new File(directory);
			System.out.println("-->Directory: " + dir.getAbsolutePath());
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
	 * Load Cluster from given file
	 * @param fileName String name of file
	 */
	protected void loadCluster(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					new File(fileName + "Patterns.data")));
			patterns = (Cluster) (in.readObject());
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
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
		int turn = board.getTurn();
		int currentTurn = 0;
		Board[][][] patternBoard = new Board[4][2][2];
		for (int rotation = 0; rotation < 4; rotation++) {
			for (int reflection = 0; reflection < 2; reflection++) {
				for (int color = 0; color < 2; color++) {
					patternBoard[rotation][reflection][color] = new Board();
				}
				patternBoard[rotation][reflection][WHITE].setColorToPlay(WHITE);
			}
		}
		while (currentTurn < turn) {
			int goodMove = board.getMove(currentTurn);
			if (isOnBoard(goodMove)) {
				// Choose a random move to store as bad
				IntSet possibleMoves = patternBoard[0][0][0].getVacantPoints();
				int badMove;
				do {
					badMove = possibleMoves.get(random.nextInt(possibleMoves
							.size()));
				} while (!patternBoard[0][0][0].isLegal(badMove)
						|| badMove == goodMove);
				// Store in all 16 rotations, reflections, and color inversions
				for (int rotation = 0; rotation < 4; rotation++) {
					for (int reflection = 0; reflection < 2; reflection++) {
						for (int color = 0; color < 2; color++) {
							for(int radius = 1; radius<= MAX_PATTERN_RADIUS; radius++){
								String key = patternBoard[rotation][reflection][color].printPattern(radius, goodMove,false);
								if(winRateMap[radius][color].containsKey(key)){
									float tempWinRate = winRateMap[radius][color].get(key);
									long tempCount = countMap[radius][color].get(key);
									winRateMap[radius][color].put(key, ((tempWinRate * tempCount) + 1) / (tempCount + 1));
									countMap[radius][color].put(key, tempCount+1);
								} else{
									winRateMap[radius][color].put(key, 1.0f);
									countMap[radius][color].put(key, 1L);
								}
								key = patternBoard[rotation][reflection][color].printPattern(radius, badMove,false);
								if(winRateMap[radius][color].containsKey(key)){
									float tempWinRate = winRateMap[radius][color].get(key);
									long tempCount = countMap[radius][color].get(key);
									winRateMap[radius][color].put(key, ((tempWinRate * tempCount) + 0) / (tempCount + 1));
									countMap[radius][color].put(key, tempCount+1);
								} else{
									winRateMap[radius][color].put(key, 0.0f);
									countMap[radius][color].put(key, 1L);
								}
							}
						}
						goodMove = reflect(goodMove);
						badMove = reflect(badMove);
					}
					goodMove = rotate(goodMove);
					badMove = rotate(badMove);
				}
			}
			// Play the move
			for (int rotation = 0; rotation < 4; rotation++) {
				for (int reflection = 0; reflection < 2; reflection++) {
					for (int color = 0; color < 2; color++) {
						patternBoard[rotation][reflection][color]
								.play(goodMove);
					}
					if (isOnBoard(goodMove))
						goodMove = reflect(goodMove);
				}
				if (isOnBoard(goodMove))
					goodMove = rotate(goodMove);
			}
			currentTurn++;
		}
	}

}
