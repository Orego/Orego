package edu.lclark.orego.feature;

import java.io.Serializable;
import java.util.Arrays;

import edu.lclark.orego.core.Color;
import edu.lclark.orego.core.CoordinateSystem;
import static edu.lclark.orego.core.CoordinateSystem.*;

/** Tables for Last Good Reply with Forgetting. */
@SuppressWarnings("serial")
public final class LgrfTable implements Serializable{
	
	/** Entry [c][i] is the best reply for color c to move i (or NO_POINT if there is none). */
	private final short[][] replies1;
	
	/** Entry [c][i][j] is the best reply for color c to moves i, j (or NO_POINT if there is none). */
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
	
	public void update(Color colorToPlay, boolean playoutWon, short penultimateMove, short previousMove, short reply){
		if (reply != PASS) {
			if (playoutWon) {
				replies1[colorToPlay.index()][previousMove] = reply;
				replies2[colorToPlay.index()][penultimateMove][previousMove] = reply;
			} else {
				if (replies1[colorToPlay.index()][previousMove] == reply) {
					replies1[colorToPlay.index()][previousMove] = NO_POINT;
				}
				if (replies2[colorToPlay.index()][penultimateMove][previousMove] == reply) {
					replies2[colorToPlay.index()][penultimateMove][previousMove] = NO_POINT;
				}
			}
		}
	}
	
	public short getFirstLevelReply(Color color, short previousMove){
		return replies1[color.index()][previousMove];
	}
	
	public short getSecondLevelReply(Color color, short penultimateMove, short previousMove){
		return replies2[color.index()][penultimateMove][previousMove];
	}

}
