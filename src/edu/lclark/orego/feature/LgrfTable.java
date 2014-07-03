package edu.lclark.orego.feature;

import java.io.Serializable;
import java.util.Arrays;

import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.CoordinateSystem.*;

@SuppressWarnings("serial")
public final class LgrfTable implements Serializable{
	
	private final short[][] replies1;
	
	private final short[][][] replies2; 
	
	
	public void clear(){
		for(short[] array : replies1){
			Arrays.fill(array, NO_POINT);
		}
		for(short[][] array : replies2){
			for(short[] array2 : array){
				Arrays.fill(array2, NO_POINT);
			}
		}
	}
	
	public LgrfTable(CoordinateSystem coords){
		replies1 = new short[2][coords.getFirstPointBeyondBoard()];
		replies2 = new short[2][coords.getFirstPointBeyondBoard()][coords.getFirstPointBeyondBoard()];
	}
	
	public void update(Color colorToPlay, boolean playoutWon, short antepenultimateMove, short previousMove, short move){
		if (move != PASS) {
			if (playoutWon) {
				replies1[colorToPlay.index()][previousMove] = move;
				replies2[colorToPlay.index()][antepenultimateMove][previousMove] = move;
			} else {
				if (replies1[colorToPlay.index()][previousMove] == move) {
					replies1[colorToPlay.index()][previousMove] = NO_POINT;
				}
				if (replies2[colorToPlay.index()][antepenultimateMove][previousMove] == move) {
					replies2[colorToPlay.index()][antepenultimateMove][previousMove] = NO_POINT;
				}
			}
		}
	}
	
	public short getFirstLevelReply(Color color, short p){
		return replies1[color.index()][p];
	}
	
	public short getSecondLevelReply(Color color, short firstMove, short secondMove){
		return replies2[color.index()][firstMove][secondMove];
	}

}
