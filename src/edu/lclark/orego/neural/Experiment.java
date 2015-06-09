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
		int size = 1593, traininglimit = 800, testnumber = size - traininglimit, updates = 10000;
		double[][] training = new double[traininglimit][256];
		double[][] trainingCorrect = new double[traininglimit][10];
		double[][] testing = new double[testnumber][256];
		double[][] testingCorrect = new double[testnumber][10];
		Scanner reader = new Scanner(new File("HandwrittenNumbers.txt"));

		// Input training data
		for (int i = 0; i < traininglimit; i++) {
			for (int j = 0; j < 256; j++) {
				training[i][j] = reader.nextDouble();
			}
			for (int j = 0; j < 10; j++) {
				trainingCorrect[i][j] = reader.nextDouble();
			}
		}
		//Input testing data
		for (int i = 0; i < testnumber; i++) {
			for (int j = 0; j < 256; j++) {
				testing[i][j] = reader.nextDouble();
			}
			for (int j = 0; j < 10; j++) {
				testingCorrect[i][j] = reader.nextDouble();
			}
		}
		
		reader.close();
		
		for (int z = 0; z < updates; z++) {
			// Training
			int k = (int) (Math.random() * traininglimit);
			handwriting.train(trainingCorrect[k], training[k]);

			//Test training data to find guess
			double trainingdataccuracy = 0;
			for (int i = 0; i < traininglimit; i++) {
				double[] output = handwriting.test(training[i]);
				double max = 0;
				int guess = 0;
				for (int j = 0; j < output.length; j++) {
					if (max < output[j]) {
						max = output[j];
						guess = j;
					}
				}

				//See if guess matches correct answer
				for (int j = 0; j < 10; j++) {
					if (trainingCorrect[i][j] == 1.0) {
						if (j == guess) {
							trainingdataccuracy++;
						}
						break;
					}
				}
			}
			//System.out.println(trainingdataccuracy / traininglimit);
			
			//Test testing data to find guess
			double testingaccuracy = 0;
			for (int i = 0; i < testnumber; i++) {
				double[] output = handwriting.test(testing[i]);
				double max = 0;
				int guess = 0;
				for (int j = 0; j < output.length; j++) {
					if (max < output[j]) {
						max = output[j];
						guess = j;
					}
				}

				//See if guess matches correct answer
				for (int j = 0; j < 10; j++) {
					if (testingCorrect[i][j] == 1.0) {
						if (j == guess) {
							testingaccuracy++;
						}
						break;
					}
				}	
			}
			System.out.println(z + ":\t"+trainingdataccuracy / traininglimit +"\t"+ testingaccuracy / testnumber);
		}

	}

	private static void printNumber(int j, double[][] solution, double[][] data) {
		for (int i = 0; i < 10; i++) {
			if (solution[j][i] == 1.0) {
				System.out.println(i + " =");
			}
		}
		for (int i = 0; i < 256; i++) {
			if (data[j][i] == 1.0) {
				System.out.print("*");
			} else {
				System.out.print(" ");
			}
			if ((i % 16 == 15)) {
				System.out.println();
			}
		}

	}

}
