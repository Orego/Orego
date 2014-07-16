package edu.lclark.orego.patterns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class ShapeExtractor extends PatternExtractor{
	
	private ShapeTable shapeTable;
	
	public static void main(String[] args) {
		ShapeExtractor extractor = new ShapeExtractor();
		 extractor.buildPatternData(new File(
		 "/Network/Servers/maccsserver.lclark.edu/Users/slevenick/Desktop/patternfiles"), "patterns/patterns5x5");
		 
	}
	
	public ShapeExtractor(){
		shapeTable = new ShapeTable();
	}
	
	@Override
	protected void buildPatternData(File inputFile, String outputFileName) {
		analyzeFiles(inputFile);
		try (FileOutputStream out = new FileOutputStream(outputFileName);
				ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(shapeTable);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	protected void updateTables(boolean winner, short move){
		long hash = PatternFinder.getHash(getBoard(), move, 25);
		shapeTable.update(hash, winner);
	}

}
