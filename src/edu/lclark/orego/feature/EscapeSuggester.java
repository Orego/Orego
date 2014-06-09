package edu.lclark.orego.feature;

import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;
import static edu.lclark.orego.core.NonStoneColor.*;
import static edu.lclark.orego.core.CoordinateSystem.*;

public class EscapeSuggester implements Suggester {
	
	private final Board board;
	
	private final AtariObserver atariObserver;
	
	private final CoordinateSystem coords;
	
	/**
	 * A list of all of the moves for the current player to play that will
	 * allow a group to escape from atari
	 */
	private final ShortSet movesToEscape;
	
	private final ShortSet tempLiberties;
	
	public EscapeSuggester(Board board, AtariObserver atariObserver){
		this.board = board;
		this.coords = board.getCoordinateSystem();
		this.atariObserver = atariObserver;
		tempLiberties = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		movesToEscape = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
	}

	@Override
	public ShortSet getMoves() {
		ShortSet chainsInAtari = atariObserver.getChainsInAtari(board.getColorToPlay());
		for(int i = 0; i < chainsInAtari.size(); i++){
			tempLiberties.clear();
			short p = board.getLiberties(chainsInAtari.get(i)).get(0);
			if(board.getNeighborsOfColor(p, VACANT)>=2){
				movesToEscape.add(p);				
			} else {
				short[] neighbors = coords.getNeighbors(p);
				if(board.getLiberties(p).size() !=0){
					tempLiberties.add(board.getLiberties(p).get(0));
				}
				for(int j = FIRST_ORTHOGONAL_NEIGHBOR; j<=LAST_ORTHOGONAL_NEIGHBOR; j++){
					ShortSet neighborsLiberties = board.getLiberties(neighbors[j]);
					if(neighborsLiberties != null && neighborsLiberties.size()>1 && (board.getColorAt(neighbors[j]) == board.getColorToPlay())){
						for(int k = 0; k < neighborsLiberties.size(); k++){
							tempLiberties.add(neighborsLiberties.get(k));
							if(tempLiberties.size() == 3){
								movesToEscape.add(p);
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
		return movesToEscape;
	}

}
