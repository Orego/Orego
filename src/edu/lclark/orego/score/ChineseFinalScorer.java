package edu.lclark.orego.score;

import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.StoneColor.*;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;

@SuppressWarnings("serial")
public class ChineseFinalScorer implements Scorer {

	private Board board;

	private CoordinateSystem coords;

	private Color colorToScore;

	private ShortSet visitedPoints;
	
	private boolean validTerritory;

	/**
	 * The amount of komi that white gets. For speed this is stored as a
	 * negative number
	 */
	private double komi;

	public ChineseFinalScorer(Board board, double komi) {
		this.board = board;
		this.komi = -komi;
		coords = board.getCoordinateSystem();
		visitedPoints = new ShortSet(coords.getFirstPointBeyondBoard());
	}

	@Override
	public double score() {
		double result = komi;
		for (short p : coords.getAllPointsOnBoard()) {
			Color color = board.getColorAt(p);
			if (color == BLACK) {
				result++;
			} else if (color == WHITE) {
				result--;
			}

		}
		
		ShortSet vacantPoints = board.getVacantPoints();
		for (int i = 0; i < vacantPoints.size(); i++) {
			short p = vacantPoints.get(i);
			if(visitedPoints.contains(p)){
				continue;
			}
			colorToScore = VACANT;
			validTerritory = true;
			visitedPoints.add(p);
			int territory = searchNeighbors(p);
			if(validTerritory){
				if(colorToScore == WHITE){
					result -= territory;
				}
				else{
					result += territory;
				}
			}
		}
		return result;
	}

	private int searchNeighbors(short p) {
		int result = 1;
		short[] neighbors = coords.getNeighbors(p);
		for (int i = coords.FIRST_ORTHOGONAL_NEIGHBOR; i <= coords.LAST_ORTHOGONAL_NEIGHBOR; i++) {
			short n = neighbors[i];
			Color nColor = board.getColorAt(n);
			if(nColor == OFF_BOARD){
				continue;
			}
			
			if(colorToScore == VACANT){
				colorToScore = nColor;
			}
			if (nColor == VACANT) {
				if (!visitedPoints.contains(n)) {
					visitedPoints.add(n);
					result += searchNeighbors(n);
				}
			} else if (nColor == colorToScore) {
				continue;
			} else {
				System.out.println(nColor);
				System.out.println(colorToScore);
				System.out.println("Not valid territory");
				validTerritory = false;
			}
		}
		return result;

	}

	@Override
	public Color winner() {
		double score = score();
		if (score > 0) {
			return BLACK;
		} else if (score < 0) {
			return WHITE;
		}
		return VACANT;
	}

}
