package orego.experiment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static orego.core.Coordinates.*;
import static orego.core.Colors.*;

import orego.heuristic.PatternHeuristic;
import orego.heuristic.PatternHeuristicPatterns;
import orego.mcts.WeightTrainingPlayer;
import orego.patterns.Pattern;
import orego.play.Player;
import orego.play.UnknownPropertyException;
import orego.util.Pair;

public class WeightTrainer {
	
	double learningRate, decayRate, winRate;
	int learningIterations, gradientIterations;
	int patternsCount = 1;
	WeightTrainingPlayer trainingPlayer;
	Player referencePlayer;
	String outputFilename;
	
	public static void main(String[] args) {
		if(args.length != 6) {
			System.out.println("Usage: WeightTrainer learning_rate decay_rate learning_iterations gradient_iterations initial_pattern_count output_file");
			return;
		}
		
		double learningRate = Double.parseDouble(args[0]);
		double decayRate = Double.parseDouble(args[1]);
		int learningIterations = Integer.parseInt(args[2]);
		int gradientIterations = Integer.parseInt(args[3]);
		int initialPatternCount = Integer.parseInt(args[4]);
		String outputFile = args[5];
		
		WeightTrainer trainer = new WeightTrainer(learningRate, decayRate, learningIterations, gradientIterations, initialPatternCount, outputFile);
		try {
			trainer.train();
		} catch (Exception e) {
			System.err.println("Error during training:");
			e.printStackTrace();
		}
	}
	
	public WeightTrainer(double learningRate, double decayRate, int learningIterations, int gradientIterations, int initialPatternCount, String outputFile) {
		this.learningRate = learningRate;
		this.decayRate = decayRate;
		this.learningIterations = learningIterations;
		this.gradientIterations = gradientIterations;
		this.outputFilename = outputFile;
		this.patternsCount = initialPatternCount;
		double[] weights = null;
		try {
			ObjectInputStream stream = new ObjectInputStream(new FileInputStream(this.outputFilename));
			weights = (double[])stream.readObject();
			stream.close();
		} catch (Exception e) {
			System.out.println("Could not open weights file.");
		}
		
		trainingPlayer = new WeightTrainingPlayer();
		try {
			trainingPlayer.setProperty("heuristics", "Escape@20:Capture@20");
		} catch (UnknownPropertyException e) {
			System.err.println("Could not set heuristics for the training player.");
			e.printStackTrace();
			System.exit(1);
		}
		trainingPlayer.reset();
		if(weights != null) {
			trainingPlayer.setWeights(weights);
		}
		referencePlayer = new Player();
		try {
			referencePlayer.setProperty("heuristics", "Escape@20:Capture@20:Pattern@20");
			referencePlayer.setProperty("heuristic.Pattern.numberOfGoodPatterns", Integer.toString(patternsCount));
		} catch (UnknownPropertyException e) {
			System.err.println("Could not set heuristics for the reference player.");
			e.printStackTrace();
			System.exit(1);
		}
		referencePlayer.reset();
	}
	
	private void train() throws FileNotFoundException, IOException, UnknownPropertyException {
		for(int trainingIdx = 0; trainingIdx < this.learningIterations; trainingIdx++) {
			if(winRate > 0 && patternsCount < PatternHeuristicPatterns.ALL_GOOD_PATTERNS.length) {
				patternsCount++;
				System.out.println(String.format("Adding another pattern to the opponent. Now using %d patterns.", patternsCount));
				referencePlayer.setProperty("heuristic.Pattern.numberOfGoodPatterns", Integer.toString(patternsCount));
			}
			System.out.println("Learning rate: " + learningRate);
			Map<Character, Double> grad = estimateGradient();
			for(char feature : grad.keySet()) {
				double newWeight = trainingPlayer.getWeight(feature) + learningRate * grad.get(feature);
				trainingPlayer.setWeight(feature, Math.min(newWeight, 700.0));
				//trainingPlayer.setWeight(inverse(feature), newWeight);
			}
			learningRate = decayRate * learningRate;
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
			int winner = this.playOneGame(true);
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
		winRate = totalReward / this.gradientIterations;
		System.out.println("Win rate: " + winRate);
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

