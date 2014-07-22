package edu.lclark.orego.patterns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class ShapeExtractor extends PatternExtractor{
	
	private ShapeTable shapeTable;
	
	public static void main(String[] args) {
		ShapeExtractor extractor = new ShapeExtractor(true, 0.99f);
		 extractor.buildPatternData(new File(
		 "/Network/Servers/maccsserver.lclark.edu/Users/mdreyer/Desktop/KGS Files"));	
		ShapeTable table = new ShapeTable("patterns/patterns3x3-SHAPE-sf99.data");
		table.getRates();
	}
	
	public ShapeExtractor(boolean verbose, float scalingFactor){
		super(verbose);
		shapeTable = new ShapeTable(scalingFactor);		
	}
	
	@Override
	protected void buildPatternData(File inputFile) {
		analyzeFiles(inputFile);
		try (FileOutputStream out = new FileOutputStream("patterns/patterns5x5.data");
				ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(shapeTable.getWinRateTables());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	protected void updateTables(boolean winner, short move){
		long hash = PatternFinder.getHash(getBoard(), move, 8);
		shapeTable.update(hash, winner);
	}

}
