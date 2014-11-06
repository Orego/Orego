package edu.lclark.orego.patterns;

import java.io.*;
import java.util.Arrays;

/** A class for storing win rates for pattern hashes. */
@SuppressWarnings("serial")
public final class ShapeTable implements Serializable {

	private final float[][] winRateTables;

	private float scalingFactor;

	/** This creates a blank Shape Table with every entry equal to 0.5. */
	public ShapeTable() {
		this(0.99f);
	}

	/** This creates a ShapeTable filled with data from the specified file. */
	public ShapeTable(String filePath, float scalingFactor) {
		float[][] fake = null;
		try (ObjectInputStream objectInputStream = new ObjectInputStream(
				new FileInputStream(filePath))) {
			fake = (float[][]) objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		winRateTables = fake;
		this.scalingFactor = scalingFactor;
	}

	/**
	 * Use to build pattern data with a particular scaling factor. Table is
	 * created and filled with 0.5.
	 */
	public ShapeTable(float scalingFactor) {
		this.scalingFactor = scalingFactor;
		winRateTables = new float[3][2097152];
		for (float[] table : winRateTables) {
			Arrays.fill(table, 0.5f);
		}
	}

	public void getRates() {
		// TODO What is this specific filename doing here?
		try (PrintWriter writer = new PrintWriter(new File("test-books/patterns5x5.csv"))) {
			for (float winRate : winRateTables[0]) {
				writer.println(winRate + ",");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public float getScalingFactor(){
		return scalingFactor;
	}

	public double testGetRate(int index) {
		return winRateTables[1][index];
	}

	public float[][] getWinRateTables() {
		return winRateTables;
	}

	/** Update the table with new win data for the given pattern. */
	public void update(long hash, boolean win) {
		for (int i = 0; i < 3; i++) {
			// TODO Get rid of this ridiculous magic number
			int index = (int) (hash >> (21 * i) & 2097151);
			winRateTables[i][index] = scalingFactor * winRateTables[i][index]
					+ (win ? (1.0f - scalingFactor) : 0);
		}
	}

	/** Get the win rate for a given pattern. */
	public float getWinRate(long hash) {
		float result = 0;
		for (int i = 0; i < 3; i++) {
			int index = (int) (hash >> (21 * i) & 2097151);
			result += winRateTables[i][index];
		}
		return result / 3;
	}
	
	/** Prints the win rate stored in each section of the table. */
	public void printIndividualWinRates(long hash){
		for (int i = 0; i < 3; i++) {
			int index = (int) (hash >> (21 * i) & 2097151);
			System.out.println("Section " + i + ": " + winRateTables[i][index]);
		}
	}
}
