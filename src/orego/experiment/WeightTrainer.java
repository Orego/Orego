package orego.experiment;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

import orego.mcts.WeightTrainingPlayer;
import orego.patterns.Pattern;
import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.util.Pair;

public class WeightTrainer {
	
	double learningRate;
	int learningIterations, gradientIterations;
	WeightTrainingPlayer trainingPlayer;
	Player referencePlayer;
	String outputFilename;
	
	public static void main(String[] args) {
		if(args.length != 4) {
			System.out.println("Usage: WeightTrainer learning_rate learning_iterations gradient_iterations output_file");
			return;
		}
		
		double learningRate = Double.parseDouble(args[0]);
		int learningIterations = Integer.parseInt(args[1]);
		int gradientIterations = Integer.parseInt(args[2]); 
		String outputFile = args[3];
		
		WeightTrainer trainer = new WeightTrainer(learningRate, learningIterations, gradientIterations, outputFile);
		try {
			trainer.train();
		} catch (Exception e) {
			System.err.println("Error during training:");
			e.printStackTrace();
		}
	}
	
	public WeightTrainer(double learningRate, int learningIterations, int gradientIterations, String outputFile) {
		this.learningRate = learningRate;
		this.learningIterations = learningIterations;
		this.gradientIterations = gradientIterations;
		this.outputFilename = outputFile;
		trainingPlayer = new WeightTrainingPlayer();
		trainingPlayer.reset();
		referencePlayer = new Player();
		try {
			referencePlayer.setProperty("heuristics", "Escape@20:Capture@20");
		} catch (UnknownPropertyException e) {
			System.err.println("Could not set heuristics for the reference player.");
			e.printStackTrace();
			System.exit(1);
		}
		referencePlayer.reset();
	}
	
	private void train() throws FileNotFoundException, IOException {
		for(int trainingIdx = 0; trainingIdx < this.learningIterations; trainingIdx++) {
			System.out.println("Learning rate: " + learningRate);
			Map<Character, Double> grad = estimateGradient();
			for(char feature : grad.keySet()) {
				double newWeight = trainingPlayer.getWeight(feature) + learningRate * grad.get(feature);
				trainingPlayer.setWeight(feature, newWeight);
				trainingPlayer.setWeight(inverse(feature), newWeight);
			}
			learningRate = 0.99 * learningRate;
			ObjectOutputStream stream = null;
			stream = new ObjectOutputStream(new FileOutputStream(outputFilename));
			stream.writeObject(trainingPlayer.getWeights());
			stream.close();
		}
	}
	
	private Map<Character, Double> estimateGradient() {
		double totalReward = 0.0;
		Map<Character, Double> gradientEstimate = new HashMap<Character, Double>();
		for(int runIdx = 0; runIdx < this.gradientIterations; runIdx++) {
			//System.out.println("Game: " + runIdx);
			int winner = this.playOneGame(runIdx % 2 == 0);
			double reward;
			if(winner == 0) {
				reward = 1.0;
			}
			else {
				reward = -1.0;
			}
			totalReward += reward;
			for(Pair<Character, Double> grad : trainingPlayer.getGradients()) {
				if(!gradientEstimate.containsKey(grad.fst)) {
					gradientEstimate.put(grad.fst, 0.0);
				}
				double current = gradientEstimate.get(grad.fst);
				gradientEstimate.put(grad.fst, current + reward * grad.snd);
			}
		}
		for(char idx : gradientEstimate.keySet()) {
			gradientEstimate.put(idx, gradientEstimate.get(idx) / this.gradientIterations);
		}
		System.out.println("Win rate: " + totalReward / this.gradientIterations);
		return gradientEstimate;
	}
	
	private int playOneGame(boolean trainingBlack) {
		trainingPlayer.reset();
		referencePlayer.reset();
		Player[] players = new Player[] {trainingPlayer, referencePlayer};
		int current = trainingBlack ? 0 : 1;
		while(trainingPlayer.getBoard().getPasses() < 2) {
			int pt = players[current].bestMove();
			if(pt == RESIGN) {
				return 1 - current;
			}
			trainingPlayer.acceptMove(pt);
			referencePlayer.acceptMove(pt);
			current = 1 - current;
		}
		int winningColor = trainingPlayer.getBoard().finalWinner();
		if(trainingBlack) {
			return winningColor;
		}
		else {
			return 1 - winningColor;
		}
	}

	private static char inverse(char pattern) {
		int[] parts = Pattern.neighborhoodToArray(pattern);
		for(int idx = 0; idx < parts.length; idx++) {
			int current = parts[idx];
			if(isAPlayerColor(current)) {
				parts[idx] = opposite(current);
			}
		}
		return Pattern.arrayToNeighborhood(parts);
	}
	
}

