package orego.mcts;

import static java.lang.String.format;
import static orego.core.Coordinates.*;
import static orego.core.Coordinates.at;
import static orego.core.Colors.WHITE;
import static orego.core.Colors.BLACK;
import static orego.core.Colors.VACANT;

import java.util.Set;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.Writer;

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
		covarianceData = new int[getFirstPointBeyondBoard()][4];
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
		
		for(int p = 0; p < getFirstPointBeyondBoard(); p++) {
			
			// Count the number of adjacent black or white points
			int whiteNeighbors = 0;
			int blackNeighbors = 0;
			
			for(int n = 0; n < 4; n++) {
				int color = board.getColor(getNeighbors(p)[n]);
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

	/** Get the maximum covariance value for the chain that the specified point belongs to. */
	public double getCovarianceOverChain(int p) {
		if(covarianceData == null) return Double.NaN;
		
		double maxCov = getCovariance(p);
		
		if(getBoard().getColor(p) == VACANT) return maxCov; 
		
		int chainId = getBoard().getChainId(p);
		for(int idx = 0; idx < getFirstPointBeyondBoard(); idx++) {
			if(getBoard().getChainId(idx) == chainId && getCovariance(idx) > maxCov) {
				maxCov = getCovariance(idx);
			}
		}
		return maxCov;
		
	}
	
	@Override
	public Set<String> getCommands() {
		Set<String> result = super.getCommands();
		result.add("gogui-covariance");
		result.add("csv-covariance");
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
		if(command.equals("csv-covariance")) {
			String filename = arguments.nextToken();
			try {
				return writeCovarianceDataToFile(filename);
			} catch (Exception e) {
				return "error";
			}
		}
		return super.handleCommand(command, arguments);
	}
	
	// Display the covariance values as influence indicators in gogui
	protected String goguiCovariance() {
		// Find the max covariance of any move
		double max = 0;
		double min = Double.POSITIVE_INFINITY;
		for (int p : getAllPointsOnBoard()) {
			double cov = getCovarianceOverChain(p);
			
			if (cov > max) {
				max = cov;
			}
			if(cov < min) {
				min = cov;
			}
		}
		// Display normalized covariance through each move
		String result = "INFLUENCE";
		for (int p : getAllPointsOnBoard()) {
			result += format(" %s %.3f", pointToString(p), (getCovarianceOverChain(p) - min) / (max - min));
		}
		return result;
	}
	
	protected String writeCovarianceDataToFile(String filename) throws IOException {
		Writer fileWriter = null;
		try {
			fileWriter = new OutputStreamWriter(new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
			return "File could not be opened.";
		}
		try {
			writeCovarianceDataToStream(fileWriter);
		} catch (Exception e) {
			return "Could not write data.";
		} finally {
			fileWriter.close();
		}
		return "Success";
	}
	
	protected void writeCovarianceDataToStream(Writer stream) throws IOException {
		for(int row = 0; row < getBoardWidth(); row++) {
			StringBuilder lineBuilder = new StringBuilder();
			for(int col = 0; col < getBoardWidth(); col++) {
				int p = at(row, col);
				lineBuilder.append(getCovarianceOverChain(p));
				if(col < getBoardWidth() - 1) lineBuilder.append(", ");
			}
			lineBuilder.append('\n');
			stream.write(lineBuilder.toString());
		}
	}
}
