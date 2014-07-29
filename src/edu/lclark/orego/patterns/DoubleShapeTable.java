package edu.lclark.orego.patterns;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import edu.lclark.orego.core.Board;

public class DoubleShapeTable {

	private ShapeTable[] tables;

	public DoubleShapeTable(String filePath) {
		try (ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(filePath))) {
			tables = (ShapeTable[]) objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public DoubleShapeTable(float scalingFactor) {
		tables = new ShapeTable[2];
		for(int i = 0; i < tables.length; i++){
			tables[i] = new ShapeTable(scalingFactor);
		}
	}
	
	public void update(Board board, short move, boolean winner, int minStones){
		for(int i = 0; i < tables.length; i++){
			long hash = PatternFinder.getHash(board, move, minStones, i);
			tables[i].update(hash, winner);
		}
	}
	
	ShapeTable[] getTables(){
		return tables;
	}
	
	float getWinRate(Board board, short move, int minStones){
		float winRate = 0.0f;
		for(int i = 0; i < tables.length; i++){
			long hash = PatternFinder.getHash(board, move, minStones, i);
			winRate += tables[i].getWinRate(hash);
		}
		return winRate / tables.length;
	}
	
	float getScalingFactor(){
		return tables[0].getScalingFactor();
	}

}
