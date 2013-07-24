package orego.book;

import static orego.core.Coordinates.NO_POINT;
import static orego.core.Coordinates.getBoardWidth;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import orego.core.Board;
import orego.shape.Cluster;
import orego.util.IntSet;
import ec.util.MersenneTwisterFast;

public class PatternBook implements OpeningBook {

	private Cluster patterns;
	
	private MersenneTwisterFast random;

	public PatternBook() {
		loadCluster("SgfFiles" + File.separator+getBoardWidth()+File.separator  + "Patternsr4t4b16.data");
		random = new MersenneTwisterFast();
	}
	
	/**
	 * Constructor for testing
	 * @param file
	 * @param seed
	 */
	public PatternBook(String file, int seed){
		loadCluster(file);
		random = new MersenneTwisterFast(seed);
	}
	
	/**
	 * Given file path (starting with "SgfFiles" or "SgfTestFiles" and ending in the name of the file the cluster is stored in,
	 * set file to store from and load Cluster.
	 * @param file
	 */
	protected void loadCluster(String file) {
		try {
			ObjectInputStream in = new ObjectInputStream(
					new FileInputStream(new File(
							orego.experiment.Debug.OREGO_ROOT_DIRECTORY + file)));
			patterns = (Cluster) (in.readObject());
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		patterns.setWeights(1,9,36,100);
	}

	@Override
	public int nextMove(Board board) {
		if (board.getTurn() < 30) {
			double best = 0;
			int result = NO_POINT;
			IntSet vacantPoints = board.getVacantPoints();
			int start;
			start = random.nextInt(vacantPoints.size());
			int i = start;
			do {
				int move = vacantPoints.get(i);
				double searchValue = patterns.getWinRate(board, move);
				if (searchValue > best) {
					if (board.isFeasible(move) && board.isLegal(move)) {
						best = searchValue;
						result = move;
					}
				}
				// The magic number 457 is prime and larger than
				// vacantPoints.size().
				// Advancing by 457 therefore skips "randomly" through the
				// array,
				// in a manner analogous to double hashing.
				i = (i + 457) % vacantPoints.size();
			} while (i != start);
			return result;
		} else
			return NO_POINT;
	}

}
