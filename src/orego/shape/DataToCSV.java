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
import static orego.experiment.ExperimentConfiguration.SGF_DIRECTORY;

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
public class DataToCSV {

	/** Hash table containing the actual win rates for each pattern. */
	private HashMap<DensePattern, Float> winRateMap;

	/** Hash table containing the actual count for each pattern. */
	private HashMap<DensePattern, Long> countMap;

	private Cluster patterns;

	

	public static void main(String[] args) {
		// System.out.println(new File(inputDirectory).getAbsolutePath());
		DataToCSV data = new DataToCSV();
		for (int i = 0; i < PatternExtractor.PARAMETERS.length; i++) {
			data.run(
					"SgfFiles",PatternExtractor.PARAMETERS[i][0], PatternExtractor.PARAMETERS[i][1]);
		}
	}

	/**
	 * Extracts patterns from all files in a directory.
	 * 
	 * @param directory
	 *            Directory (within OREGO_ROOT_DIRECTORY) to store output and
	 *            load Cluster from, usually "SgfFiles" or "SgfTestFiles".
	 */
	@SuppressWarnings("unchecked")
	public void run(String directory, int tables, int bits) {
		directory = orego.experiment.Debug.OREGO_ROOT_DIRECTORY + directory
				+ File.separator;
		try {
			patterns = (Cluster) loadFromFile(directory + "Patternsr" + 4 + "t"
					+ tables + "b" + bits + ".data");
			directory += getBoardWidth() + File.separator;

			for (int radius = DataMiner.MIN_PATTERN_RADIUS; radius <= DataMiner.MAX_PATTERN_RADIUS; radius++) {
				winRateMap = (HashMap<DensePattern, Float>) loadFromFile(directory
						+ "Actual-win-rate"+radius+".data");
				countMap = (HashMap<DensePattern, Long>) loadFromFile(directory
						+ "Actual-count"+radius+".data");
				PrintWriter bw = new PrintWriter(new FileWriter(new File(
						directory + "results_r" + radius + "t"+tables+"b"+bits+".csv")));
				System.out.println("writing out to "+directory + "results_r" + radius + "t"+tables+"b"+bits+".csv");
				StringBuilder output = new StringBuilder("");
				for (DensePattern key : winRateMap.keySet()) {
					String key2 = key.toString();
					long hash = keyToHash(key2, radius);
					output.append(key2);
					output.append(',');
					output.append(hash);
					output.append(',');
					output.append(winRateMap.get(key));
					output.append(',');
					output.append(countMap.get(key));
					output.append(',');
					output.append(patterns.getPatternWinRate(hash, BLACK,
							radius));
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
		for (int i = 0; i < key.length(); i++) {
			if (charToColor(key.charAt(i)) != VACANT)
				hash ^= Board.PATTERN_ZOBRIST_HASHES[radius][charToColor(key
						.charAt(i))][i];
		}
		return hash;
	}

	/**
	 * Load object from given file
	 * 
	 * @param filename
	 *            String name of file
	 */
	protected Object loadFromFile(String filename) {
		Object object = null;
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					new File(filename)));
			object = in.readObject();
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return object;
	}
}
