package edu.lclark.orego.neural;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Experiment {

	public static void main(String[] args) throws FileNotFoundException {
		// Network or = new Network();
		// double[][] training = {{0,0}, {1,0}, {0,1}, {1,1}};
		// double[] trainingCorrect = {0, 1, 1, 1};
		// int updates = 1000;
		// for (int i = 0; i < updates; i++) {
		// int k = (int)(Math.random() * training.length);
		// or.train(trainingCorrect[k], training[k]);
		// for (int j = 0; j < training.length; j++){
		// System.out.print(or.test(training[j]) + "\t");
		// }
		// System.out.println();
		// }
		//
		// Network xor = new Network(2, 2, 2);
		// double[][] training = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
		// double[][] trainingCorrect = { { 0 }, { 1 }, { 1 }, { 0 } };
		// int updates = 10000;
		// for (int i = 0; i < updates; i++) {
		// int k = (int) (Math.random() * training.length);
		// xor.train(trainingCorrect[k], training[k]);
		// for (int z = 0; z < trainingCorrect[0].length; z++) {
		// for (int j = 0; j < training.length; j++) {
		// System.out.print(xor.test(training[j])[z] + "\t");
		// }
		// System.out.println();
		// }
		//
		// }
		// }

		// Declare stuff
		Network handwriting = new Network(256, 10, 1, 10);
		int size = 800;
		double[][] training = new double[size][256];
		double[][] trainingCorrect = new double[size][10];
		int updates = 10000, traininglimit = 800;
		Scanner reader = new Scanner(new File("HandwrittenNumbers.txt"));

		// Input training data
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < 256; j++) {
				training[i][j] = reader.nextDouble();
			}
			for (int j = 0; j < 10; j++) {
				trainingCorrect[i][j] = reader.nextDouble();
			}
		}
		reader.close();

		// Training
		for (int i = 0; i < updates; i++) {
			int k = (int) (Math.random() * traininglimit);
			handwriting.train(trainingCorrect[k], training[k]);
			// double[] output = handwriting.test(training[k]);
			// for (int j = 0; j < output.length; j++) {
			// System.out.print(j + ":" + output[j] + "\t");
			// }
			// System.out.println();
		}

		// Testing accuracy
		double trainingdataccuracy = 0;
		for (int i = 0; i < traininglimit; i++) {
			double[] output = handwriting.test(training[i]);
			double max = 0;
			int guess = 0;
			for (int j = 0; j < output.length; j++) {
				// System.out.print(j + ":" + output[j] + "\t");
				if (max < output[j]) {
					max = output[j];
					guess = j;
				}
			}
			// System.out.println();

			for (int j = 0; j < 10; j++) {
				if (trainingCorrect[i][j] == 1.0) {
					System.out.println(j + " = " + guess);
					if (j == guess) {
						trainingdataccuracy++;
					}
					break;
				}
			}
		}
		System.out.println(trainingdataccuracy / traininglimit);
	}

	private static double extract(double[] correct) {
		for (int i = 0; i < correct.length; i++) {
			if ((int) correct[i] == 1) {
				return i;
			}
		}
		return -1;
	}
}
