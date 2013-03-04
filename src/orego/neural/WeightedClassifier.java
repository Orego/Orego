package orego.neural;

import static orego.core.Colors.*;
import static orego.core.Coordinates.*;
import orego.core.*;

public class WeightedClassifier extends AverageClassifier{

	public WeightedClassifier(double learn, int history) {
		super(learn, history);
		double[][][][] weights = getWeights();
		for (int i = 0; i < LAST_POINT_ON_BOARD + 1; i++) {
			weights[BLACK][BIAS][0][i] = initialWeight(i, at("e5"));
			weights[WHITE][BIAS][0][i] = initialWeight(i, at("e5"));
		}
		for (int k : ALL_POINTS_ON_BOARD) {
			for (int i = 0; i < getHistory(); i++) {
				for (int j : ALL_POINTS_ON_BOARD) {
					weights[BLACK][j][i][k] = initialWeight(j, k);
					weights[WHITE][j][i][k] = initialWeight(j, k);
				}
				weights[BLACK][NO_POINT][i][k] = initialWeight(k, k);
				weights[WHITE][NO_POINT][i][k] = initialWeight(k, k);				
			}
		}
	}
	
	protected double initialWeight(int p, int q) {
		return (1.0)
		- (getDistance(p, q)) / (12);
	}

}
