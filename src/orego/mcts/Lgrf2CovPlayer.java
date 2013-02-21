package orego.mcts;

import static java.lang.String.format;
import static orego.core.Coordinates.ALL_POINTS_ON_BOARD;
import static orego.core.Coordinates.FIRST_POINT_BEYOND_BOARD;
import static orego.core.Coordinates.NEIGHBORS;
import static orego.core.Coordinates.pointToString;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.BLACK;

import java.util.Set;
import java.util.StringTokenizer;

import orego.core.Board;


public class Lgrf2CovPlayer extends Lgrf2Player {
	
	// Black controlled, black won
	private static final int BB = 0;
	// White controlled, black won
	private static final int WB = 1;
	// Black controlled, white won
	private static final int BW = 2;
	// White controlled, white won
	private static final int WW = 3;
	
	private int[][] covarianceData = null;
	
	@Override
	public int bestMove() {
		// Clear out the stored covariance data
		covarianceData = new int[FIRST_POINT_BEYOND_BOARD][4];
		// Do the actual search
		return super.bestMove();
	}
	
	@Override
	public void incorporateRun(int winner, McRunnable runnable) {
		super.incorporateRun(winner, runnable);
		
		// Look at the ending state of the playout
		Board board = runnable.getBoard();
		
		// This is the offset to add depending on whether black or white won
		int winnerAdd = board.playoutWinner() == WHITE ? 2 : 0;
		
		for(int p = 0; p < FIRST_POINT_BEYOND_BOARD; p++) {
			
			// Count the number of adjacent black or white points
			int whiteNeighbors = 0;
			int blackNeighbors = 0;
			
			for(int n = 0; n < 4; n++) {
				int color = board.getColor(NEIGHBORS[p][n]);
				if(color == WHITE) {
					whiteNeighbors++;
				}
				else if(color == BLACK) {
					blackNeighbors++;
				}
			}
			
			// If a majority was black or white, increment the correct field
			if(blackNeighbors > whiteNeighbors) {
				covarianceData[p][winnerAdd]++;
			}
			else if(whiteNeighbors > blackNeighbors) {
				covarianceData[p][winnerAdd + 1]++;
			}
			
		}
	}
	
	/** Get the covariance value for a specific point. */
	public double getCovariance(int p) {
		if(covarianceData == null) return Double.NaN;
		
		int[] d = covarianceData[p];
		return ((double) (d[BB]*d[WW] - d[BW]*d[WB])) / Math.pow((d[BB] + d[BW] + d[WB] + d[WW]), 2);
	}

	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-covariance");
		return result;
	}

	@Override
	public Set<String> getGoguiCommands() {
		Set<String> result = super.getGoguiCommands();
		result.add("gfx/Covariance/gogui-covariance");
		return result;
	}
	
	@Override
	public String handleCommand(String command, StringTokenizer arguments) {
		if(command.equals("gogui-covariance")) {
			return goguiCovariance();
		}
		return super.handleCommand(command, arguments);
	}
	
	// Display the covariance values as influence indicators in gogui
	protected String goguiCovariance() {
		// Find the max covariance of any move
		double max = 0;
		double min = Double.POSITIVE_INFINITY;
		for (int p : ALL_POINTS_ON_BOARD) {
			double cov = getCovariance(p);
			
			if (cov > max) {
				max = cov;
			}
			if(cov < min) {
				min = cov;
			}
		}
		// Display normalized covariance through each move
		String result = "INFLUENCE";
		for (int p : ALL_POINTS_ON_BOARD) {
			result += format(" %s %.3f", pointToString(p), (getCovariance(p) - min) / (max - min));
		}
		return result;
	}

	public static void main(String[] args) {
		Board b = new Board();
		for(int idx = 0; idx < b.getVacantPoints().size(); idx++) {
			int p = b.getVacantPoints().get(idx);
			System.out.println(p);
			System.out.println(b.isFeasible(p));
		}
	}
}
