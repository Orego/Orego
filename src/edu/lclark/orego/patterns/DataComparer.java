package edu.lclark.orego.patterns;

import static edu.lclark.orego.experiment.PropertyPaths.OREGO_ROOT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class DataComparer {
	
	public final static int STONES = 9;
	
	public final static int SCALING = 99;

	@SuppressWarnings({ "unchecked", "boxing" })
	public static void main(String[] args) {
		HashMap<Long, Float> realData = new HashMap<>();
		try (ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(OREGO_ROOT + "patterns/patterns" + STONES
						+ "stones-SHAPE-sf" + SCALING + ".data.real"))) {
			realData = (HashMap<Long, Float>) objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		ShapeTable table = new ShapeTable(OREGO_ROOT + "patterns/patterns" + STONES
				+ "stones-SHAPE-sf" + SCALING + ".data");

		try (PrintWriter writer = new PrintWriter(new File(
				"test-books/datacomparison-sf"+SCALING + "-stones" + STONES + ".csv"))) {
			for (Long hash : realData.keySet()) {
				writer.println(realData.get(hash) + ", " + table.getWinRate(hash));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
