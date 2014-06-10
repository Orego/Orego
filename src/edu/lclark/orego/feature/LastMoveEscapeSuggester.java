package edu.lclark.orego.feature;

import static edu.lclark.orego.core.CoordinateSystem.FIRST_ORTHOGONAL_NEIGHBOR;
import static edu.lclark.orego.core.CoordinateSystem.LAST_ORTHOGONAL_NEIGHBOR;
import static edu.lclark.orego.core.NonStoneColor.VACANT;
import edu.lclark.orego.core.Board;
import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import edu.lclark.orego.util.ShortSet;

public class LastMoveEscapeSuggester implements Suggester{
	
	private final ShortSet movesToEscape;
	
	private final LastMoveObserver lastMoveObserver;
	
	private final Board board;
	
	private final CoordinateSystem coords;
	
	private final ShortSet chainsInAtari;
	
	private final ShortSet tempLiberties;
	
	public LastMoveEscapeSuggester(Board board, LastMoveObserver lastMoveObserver){
		this.board = board;
		this.lastMoveObserver = lastMoveObserver;
		coords = board.getCoordinateSystem();
		movesToEscape = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		chainsInAtari = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
		tempLiberties = new ShortSet(board.getCoordinateSystem()
				.getFirstPointBeyondBoard());
	}

	@Override
	public ShortSet getMoves() {
		short lastMove = lastMoveObserver.getLastMove();
		movesToEscape.clear();
		if(lastMove == coords.NO_POINT){
			return movesToEscape;
		}
		short[] neighbors = coords.getNeighbors(lastMove);
		for(int i = FIRST_ORTHOGONAL_NEIGHBOR; i<=LAST_ORTHOGONAL_NEIGHBOR; i++){
			if(board.getColorAt(neighbors[i])==board.getColorToPlay()){
				if(board.getLiberties(board.getChainRoot(neighbors[i])).size()==1){
					chainsInAtari.add(board.getChainRoot(neighbors[i]));
				}
			}
		}
		
		for(int i = 0; i < chainsInAtari.size(); i++){
			tempLiberties.clear();
			short p = board.getLiberties(chainsInAtari.get(i)).get(0);
			if(board.getNeighborsOfColor(p, VACANT)>=2){
				movesToEscape.add(p);				
			} else {
				escapeByMerging(p);
			}
//			escapeByCapturing(chainsInAtari.get(i));
//			alliesVisited.clear();
		}
		return movesToEscape;
	}
	
	/** Adds moves to escape from atari by capturing outside enemy stones. Called recursively on all allied stones in a chain in atari.
	 * 
	 * @param p The stone in a chain in atari that we are examining.
	 */
//	private void escapeByCapturing(short p) {
//		alliesVisited.add(p);
//		short[] neighbors = coords.getNeighbors(p);
//		ShortSet enemiesInAtari = atariObserver.getChainsInAtari(board.getColorToPlay().opposite());
//		for(int i = FIRST_ORTHOGONAL_NEIGHBOR; i<=LAST_ORTHOGONAL_NEIGHBOR; i++){
//			Color color = board.getColorAt(neighbors[i]);
//			if(color==board.getColorToPlay().opposite()){
//				if(enemiesInAtari.contains(board.getChainRoot(neighbors[i]))){
//					movesToEscape.add(board.getLiberties(neighbors[i]).get(0));
//				}
//			}
//			else if(color == board.getColorToPlay()){
//				if(!alliesVisited.contains(neighbors[i])){
//					escapeByCapturing(neighbors[i]);
//				}
//			}
//		}
//	}
	
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
