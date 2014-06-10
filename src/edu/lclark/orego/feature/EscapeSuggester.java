package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;


/** Returns a set of moves that will allow groups to escape from atari by running or merging, not by capturing. */
public class EscapeSuggester implements Suggester {
	
	private final Board board;
	
	private final AtariObserver atariObserver;
	
	private final CoordinateSystem coords;
	
	/**
	 * A list of all of the moves for the current player to play that will
	 * allow a group to escape from atari
	 */
	private final ShortSet movesToEscape;
	
	/**
	 * Keeps track of the liberties of a chain that is possible to merge with.
	 */
	private final ShortSet tempLiberties;
	
	private final ShortSet enemiesVisited;
	
	private final ShortSet alliesVisited;

	public EscapeSuggester(Board board, AtariObserver atariObserver){
		this.board = board;
		this.coords = board.getCoordinateSystem();
		this.atariObserver = atariObserver;
		tempLiberties = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		movesToEscape = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		alliesVisited = new ShortSet(board.getCoordinateSystem().getFirstPointBeyondBoard());
		enemiesVisited = new ShortSet(board.getCoordinateSystem().getFirstPointBeyondBoard());
	}

	@Override
	public ShortSet getMoves() {
		movesToEscape.clear();
		ShortSet chainsInAtari = atariObserver.getChainsInAtari(board.getColorToPlay());
		for(int i = 0; i < chainsInAtari.size(); i++){
			tempLiberties.clear();
			short p = board.getLiberties(chainsInAtari.get(i)).get(0);
			if(board.getNeighborsOfColor(p, VACANT)>=2){
				movesToEscape.add(p);				
			} else {
				escapeByMerging(p);
			}
			escapeByCapturing(chainsInAtari.get(i));
			alliesVisited.clear();
		}
		return movesToEscape;
	}

	/** Adds moves to escape from atari by capturing outside enemy stones. Called recursively on all allied stones in a chain in atari.
	 * 
	 * @param p The stone in a chain in atari that we are examining.
	 */
	private void escapeByCapturing(short p) {
		alliesVisited.add(p);
		short[] neighbors = coords.getNeighbors(p);
		ShortSet enemiesInAtari = atariObserver.getChainsInAtari(board.getColorToPlay().opposite());
		for(int i = FIRST_ORTHOGONAL_NEIGHBOR; i<=LAST_ORTHOGONAL_NEIGHBOR; i++){
			Color color = board.getColorAt(neighbors[i]);
			if(color==board.getColorToPlay().opposite()){
				if(enemiesInAtari.contains(board.getChainRoot(neighbors[i]))){
					movesToEscape.add(board.getLiberties(neighbors[i]).get(0));
				}
			}
//			else if(color == board.getColorToPlay()){
//				if(!alliesVisited.contains(neighbors[i])){
//					escapeByCapturing(neighbors[i]);
//				}
//			}
		}
	}

	
	/** Finds moves to escape atari by merging with other chains 
	 * 
	 * @param liberty the liberty of the current chain in atari.
	 */
	private void escapeByMerging(short liberty) {
		short[] neighbors = coords.getNeighbors(liberty);
		if(board.getLiberties(liberty).size() !=0){
			tempLiberties.add(board.getLiberties(liberty).get(0));
		}
		for(int j = FIRST_ORTHOGONAL_NEIGHBOR; j<=LAST_ORTHOGONAL_NEIGHBOR; j++){
			ShortSet neighborsLiberties = board.getLiberties(neighbors[j]);
			if(neighborsLiberties != null && neighborsLiberties.size()>1 && (board.getColorAt(neighbors[j]) == board.getColorToPlay())){
				for(int k = 0; k < neighborsLiberties.size(); k++){
					tempLiberties.add(neighborsLiberties.get(k));
					if(tempLiberties.size() == 3){
						movesToEscape.add(liberty);
						break;
					}
				}
			}
			if(tempLiberties.size() == 3){
				break;
			}
		}
	}


}
