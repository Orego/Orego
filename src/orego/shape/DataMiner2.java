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
public class DataMiner2 {

	/** Hash table containing the actual win rates for each pattern. */
	private HashMap<String, Float>[][] winRateMap;
	
	/** Hash table containing the actual count for each pattern. */
	private HashMap<String, Long>[][] countMap;
	
	private Cluster patterns;
	
	private static final int MAX_PATTERN_RADIUS = 1;

	public static void main(String[] args) {
		new DataMiner2().run(PatternExtractor.TEST_GAMES_DIRECTORY,"Patterns.data");
	}

	@SuppressWarnings("unchecked")
	public DataMiner2() {
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
	 * @param clusterName Name of cluster to load (like Patternsr4t1b18, for example)
	 * @param directory Directory (within OREGO_ROOT_DIRECTORY) to store output and load Cluster from, usually "SgfFiles" or "SgfTestFiles".
	 */
	public void run(String directory, String clusterName) {
		directory = orego.experiment.Debug.OREGO_ROOT_DIRECTORY + directory
				+ File.separator + getBoardWidth() + File.separator;
		try {
			
			try {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(new File(directory + "actual-win-rate.data")));
				winRateMap = (HashMap<String, Float>[][]) in.readObject();
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			try {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(new File(directory + "actual-win-rate.data")));
				countMap = (HashMap<String, Long>[][]) in.readObject();
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			loadCluster(directory+clusterName);
			
			for(int radius = 1; radius<=MAX_PATTERN_RADIUS; radius++){
				PrintWriter bw = new PrintWriter(new FileWriter(new File(
						directory+"results_for_radius"+radius+".csv")));
				StringBuilder output = new StringBuilder("");
				for(String key : winRateMap[radius][BLACK].keySet()){
					long hash = keyToHash(key,radius);
					output.append(key);
					output.append(',');
					output.append(hash);
					output.append(',');
					output.append(winRateMap[radius][BLACK].get(key));
					output.append(',');
					output.append(countMap[radius][BLACK].get(key));
					output.append(',');
					output.append(patterns.getPatternWinRate(hash, BLACK, radius));
					output.append(',');
					output.append(patterns.getPatternCount(hash, BLACK, radius));
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
	 * Load Cluster from given file
	 * @param fileName String name of file
	 */
	protected void loadCluster(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					new File(fileName)));
			patterns = (Cluster) (in.readObject());
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
