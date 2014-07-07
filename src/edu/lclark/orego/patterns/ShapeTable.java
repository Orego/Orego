package edu.lclark.orego.patterns;

import java.util.Arrays;

public class ShapeTable {

	private final float[][] winRateTables;

	private final float scalingFactor = 0.99f;

	public ShapeTable() {
		winRateTables = new float[4][65536];
		for (float[] table : winRateTables) {
			Arrays.fill(table, 0.5f);
		}
	}

	public void update(long hash, boolean win) {
		for (int i = 0; i < 4; i++) {
			int index = (int) (hash >> (16 * i) & 65535);
			winRateTables[i][index] = win ? scalingFactor * winRateTables[i][index]
					+ (1 - scalingFactor) : scalingFactor * winRateTables[i][index];
		}
	}

	public float getWinRate(long hash) {
		float result = 0;
		for (int i = 0; i < 4; i++) {
			int index = (int) (hash >> (16 * i) & 65535);
			result += winRateTables[i][index];
		}
		return result / 4;
	}
}
