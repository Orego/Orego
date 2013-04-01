package orego.experiment;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Comparator;

import orego.patterns.Pattern;

public class WeightAnalyze {
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Usage: WeightAnalyze weights_file n_best n_worst");
			return;
		}
		
		String filename = args[0];
		double[] tempWeights = null;
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(filename));
			tempWeights = (double[])stream.readObject();
		} catch (Exception e) {
			System.out.println("Error reading file.");
			System.exit(1);
		}
		final double weights[] = tempWeights;
		Integer[] indices = new Integer[weights.length];
		for(int idx = 0; idx < indices.length; idx++) {
			indices[idx] = idx;
		}
		
		Arrays.sort(indices, new Comparator<Integer>() {

			@Override
			public int compare(Integer idx1, Integer idx2) {
				return -1*Double.compare(weights[idx1], weights[idx2]);
			}
			
		});
		
		System.out.println("Best patterns:");
		int bestCount = Integer.parseInt(args[1]);
		int printedCount = 0;
		int idx = 0;
		while(printedCount < bestCount) {
			String pattern = Pattern.neighborhoodToDiagram((char)indices[idx].intValue());
			double weight = weights[indices[idx]];
			if(weight != 0) {
				System.out.println(weight + ":\n" + pattern + "\n");
				printedCount++;
			}
			idx++;
		}
		
		System.out.println("Worst patterns:");
		int worstCount = Integer.parseInt(args[2]);
		printedCount = 0;
		idx = indices.length - 1;
		while(printedCount < worstCount) {
			String pattern = Pattern.neighborhoodToDiagram((char)indices[idx].intValue());
			double weight = weights[indices[idx]];
			if(weight != 0) {
				System.out.println(weight + ":\n" + pattern + "\n");
				printedCount++;
			}
			idx--;
		}
	}
}
