package edu.lclark.orego.patterns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

public class ShapeExtractor extends PatternExtractor {
	
	private ShapeTable shapeTable;
	
	public static void main(String[] args) {
		ShapeExtractor extractor = new ShapeExtractor(true, 0.99f);
		 extractor.buildPatternData(new File(
		 "/Network/Servers/maccsserver.lclark.edu/Users/mdreyer/Desktop/KGS Files"));
	}
	
	public ShapeExtractor(boolean verbose, float scalingFactor){
		super(verbose);
		shapeTable = new ShapeTable(scalingFactor);		
	}
	
	@Override
	void buildPatternData(File inputFile) {
		analyzeFiles(inputFile);
		try (FileOutputStream out = new FileOutputStream(OREGO_ROOT + "patterns/patterns3x3-SHAPE-sf99.data");
				ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(shapeTable.getWinRateTables());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	void updateTables(boolean winner, short move){
		// TODO Handle rotations and reflections
		// TODO Get rid of magic number
		long hash = PatternFinder.getHash(getBoard(), move, 8);
		shapeTable.update(hash, winner);
	}

}
